package de.headshotharp.mysql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.bukkit.entity.Player;

import de.headshotharp.chat.Chat;
import de.headshotharp.obj.EnchantmentItem;
import de.headshotharp.obj.ItemShopItem;
import de.headshotharp.obj.UserData;
import de.headshotharp.obj.UserTimeLine;
import de.headshotharp.plugin.PlayerInformator;

public class Database {
	MysqlConnector mysql;
	String table = "users";
	Thread thread;
	boolean reconnect = false;

	public Database(MysqlConnector mysql) {
		this.mysql = mysql;
		/*
		 * thread = new Thread(new Runnable() {
		 * 
		 * @Override public void run() { boolean isRunning = true; while (isRunning) {
		 * try { Thread.sleep(100); } catch (InterruptedException e1) { isRunning =
		 * false; break; } if (reconnect) { Database.this.mysql.reconnect(); try {
		 * Thread.sleep(10000); } catch (InterruptedException e) { isRunning = false;
		 * break; } reconnect = false; } } System.out.println(
		 * "[PlayerInformator] Database reconnection-thread closed"); } });
		 * thread.start();
		 */
	}

	public void requestReconnect() {
		reconnect = true;
	}

	public void disable() {
		thread.interrupt();
	}

	public RegistrationStatus getRegistrationStatus(String player) {
		String sql = "SELECT password FROM " + table + " WHERE name = '" + player + "'";
		ResultSet rs = mysql.query(sql);
		int c = 0;
		String pw = "";
		try {
			if (rs != null) {
				while (rs.next()) {
					pw = rs.getString("password");
					c++;
				}
			} else {
				return RegistrationStatus.ERROR;
			}
		} catch (SQLException e) {
			return RegistrationStatus.ERROR;
		}
		if (c == 0)
			return RegistrationStatus.NONE;
		if (c == 1) {
			if (pw.equals("NONE"))
				return RegistrationStatus.STARTED;
			return RegistrationStatus.FINISHED;
		}
		return RegistrationStatus.ERROR;
	}

	public ArrayList<UserData<Integer>> getRanking(String category, int limit) {
		String sql = "SELECT name, " + category + " FROM users where password not like 'NONE' ORDER BY " + category
				+ " DESC LIMIT " + limit;
		ArrayList<UserData<Integer>> list = new ArrayList<UserData<Integer>>();
		ResultSet rs = mysql.query(sql);
		try {
			while (rs.next()) {
				String name = rs.getString("name");
				int data = rs.getInt(category);
				list.add(new UserData<Integer>(name, data));
			}
			return list;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		} catch (NullPointerException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void addChatServer(String msg) {
		addChat(1, msg, 0);
	}

	public void addChatPlayer(int id, String msg) {
		addChat(id, msg, 2);
	}

	private void addChat(int id, String msg, int origin) {
		String sql = "INSERT INTO chat (userid, msg, origin) VALUES (?,?,?)";
		try {
			PreparedStatement stmt = mysql.conn.prepareStatement(sql);
			stmt.setInt(1, id);
			stmt.setString(2, msg);
			stmt.setInt(3, origin);
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.out.println("Failed to send update '" + sql + "'.");
			e.printStackTrace();
			System.out.println("Exception caught: No further problems!");
			requestReconnect();
		}
	}

	public int getLastChatId() {
		String sql = "SELECT id FROM chat ORDER BY id DESC LIMIT 1;";
		ResultSet rs = mysql.query(sql);
		int c = 0;
		int lastid = -1;
		try {
			while (rs.next()) {
				lastid = rs.getInt("id");
				c++;
			}
		} catch (SQLException e) {
			return -1;
		}
		if (c == 1)
			return lastid;
		return -1;
	}

	public ArrayList<Chat> getWebChat(int lastid) {
		String sql = "SELECT c.id, u.name, c.msg FROM chat AS c LEFT JOIN users AS u ON c.userid = u.id WHERE (origin = 1) AND (c.id > "
				+ lastid + ");";
		ResultSet rs = mysql.query(sql);
		ArrayList<Chat> chat = new ArrayList<Chat>();
		try {
			while (rs.next()) {
				int id = rs.getInt("id");
				String name = rs.getString("name");
				String msg = rs.getString("msg");
				chat.add(new Chat(id, name, msg));
			}
		} catch (SQLException e) {

		}
		return chat;
	}

	public void setLastLogin(int id) {
		String sql = "UPDATE users SET lastlogin = NOW() where id = " + id;
		mysql.queryUpdate(sql);
	}

	public boolean isOnline(Collection<? extends Player> onlinePlayers, String name) {
		for (Player player : onlinePlayers) {
			if (player.getName().equals(name))
				return true;
		}
		return false;
	}

	public void setAllOffline() {
		String sql = "UPDATE users SET online = 0";
		mysql.queryUpdate(sql);
	}

	public void updateOnlineList(Collection<? extends Player> onlinePlayers) {
		String sql = "SELECT id, name, online FROM users";
		ResultSet rs = mysql.query(sql);
		try {
			while (rs.next()) {
				int id = rs.getInt("id");
				String name = rs.getString("name");
				boolean online = rs.getInt("online") == 0 ? false : true;
				boolean isOnline = isOnline(onlinePlayers, name);
				if (online != isOnline) {
					setOnlineState(isOnline, id);
				}
			}
		} catch (SQLException e) {

		}
	}

	public void setOnlineState(boolean state, int id) {
		String sql = "UPDATE users SET online = " + (state ? 1 : 0) + " where id = " + id;
		mysql.queryUpdate(sql);
	}

	public Integer getMoney(String player) {
		String sql = "SELECT money FROM " + table + " WHERE name = '" + player + "'";
		ResultSet rs = mysql.query(sql);
		int c = 0;
		int money = 0;
		try {
			while (rs.next()) {
				money = rs.getInt("money");
				c++;
			}
		} catch (SQLException e) {
			return null;
		}
		if (c == 1) {
			return money;
		}
		return null;
	}

	public void setMoney(String player, int amount) {
		String sql = "UPDATE users SET money = " + amount + " WHERE name = '" + player + "'";
		mysql.queryUpdate(sql);
	}

	public void addMoney(String player, int amount) {
		String sql = "UPDATE users SET money = money + " + amount + " WHERE name = '" + player + "'";
		mysql.queryUpdate(sql);
	}

	public List<EnchantmentItem> getEnchantmentItemsForOrder(int id) {
		List<EnchantmentItem> list = new ArrayList<EnchantmentItem>();
		String sql = "select e.id, e.bukkitname, si.level from shopitemenchantments as si join enchantments as e on e.id = si.enchantmentid where si.shopitemid ="
				+ id;
		ResultSet rs = mysql.query(sql);
		try {
			while (rs.next()) {
				list.add(new EnchantmentItem(rs.getInt("id"), rs.getString("bukkitname"), rs.getInt("level")));
			}
		} catch (SQLException e) {
			return null;
		}
		return list;
	}

	public List<ItemShopItem> getBoughtItemShopItems(int userid) {
		List<ItemShopItem> list = new ArrayList<ItemShopItem>();
		ItemShopItem tmp;
		String sql = "SELECT si.id as itemid, sis.id as buyid, si.name, si.price, si.mc_item FROM shopitemshop AS sis JOIN shopitems AS si ON si.id = sis.itemid WHERE used = 0 AND sis.userid = "
				+ userid;
		ResultSet rs = mysql.query(sql);
		try {
			while (rs.next()) {
				tmp = new ItemShopItem(rs.getInt("buyid"), rs.getString("name"), rs.getString("mc_item"));
				tmp.setEnchantmentItems(getEnchantmentItemsForOrder(rs.getInt("itemid")));
				list.add(tmp);
			}
		} catch (SQLException e) {
			return null;
		}
		return list;
	}

	public ItemShopItem getBoughtItemShopItems(int userid, int buyid) {
		ItemShopItem item = null;
		String sql = "SELECT si.id as itemid, sis.id as buyid, si.name, si.price, si.mc_item FROM shopitemshop AS sis JOIN shopitems AS si ON si.id = sis.itemid WHERE used = 0 AND sis.userid = "
				+ userid + " AND sis.id = " + buyid;
		ResultSet rs = mysql.query(sql);
		try {
			if (rs.next()) {
				item = new ItemShopItem(rs.getInt("buyid"), rs.getString("name"), rs.getString("mc_item"));
				item.setEnchantmentItems(getEnchantmentItemsForOrder(rs.getInt("itemid")));
			}
		} catch (SQLException e) {
			return null;
		}
		return item;
	}

	public void setItemShopItemBought(int buyid) {
		String sql = "UPDATE shopitemshop SET used = 1 WHERE id = " + buyid;
		mysql.queryUpdate(sql);
	}

	public Integer getPlacedBlocks(String player) {
		String sql = "SELECT block_place FROM " + table + " WHERE name = '" + player + "'";
		ResultSet rs = mysql.query(sql);
		int c = 0;
		int block_place = 0;
		try {
			while (rs.next()) {
				block_place = rs.getInt("block_place");
				c++;
			}
		} catch (SQLException e) {
			return null;
		}
		if (c == 1) {
			return block_place;
		}
		return null;
	}

	public Integer getPlacedBlocksYesterday(int id) {
		String sql = "SELECT block_place FROM usertimeline WHERE userid = " + id + " ORDER BY timestamp DESC LIMIT 1";
		ResultSet rs = mysql.query(sql);
		int c = 0;
		int block_place = 0;
		try {
			while (rs.next()) {
				block_place = rs.getInt("block_place");
				c++;
			}
		} catch (SQLException e) {
			return null;
		}
		if (c == 1)
			return block_place;
		else if (c == 0)
			return 0;
		return null;
	}

	@Deprecated
	public void setPlacedBlocks(String player, int amount) {
		String sql = "UPDATE users SET block_place = " + amount + " WHERE name = '" + player + "'";
		mysql.queryUpdate(sql);
	}

	public void incPlacedBlocks(String player) {
		String sql = "UPDATE users SET block_place = block_place + 1 WHERE name = '" + player + "'";
		mysql.queryUpdate(sql);
	}

	@Deprecated
	public void addPlacedBlocks(String player, int amount) {
		int blocks = getPlacedBlocks(player);
		blocks += amount;
		setPlacedBlocks(player, blocks);
	}

	public void incBrokenBlocks(String player) {
		String sql = "UPDATE users SET block_break = block_break + 1 WHERE name = '" + player + "'";
		mysql.queryUpdate(sql);
	}

	public Integer getBrokenBlocks(String player) {
		String sql = "SELECT block_break FROM " + table + " WHERE name = '" + player + "'";
		ResultSet rs = mysql.query(sql);
		int c = 0;
		int block_break = 0;
		try {
			while (rs.next()) {
				block_break = rs.getInt("block_break");
				c++;
			}
		} catch (SQLException e) {
			return null;
		}
		if (c == 1) {
			return block_break;
		}
		return null;
	}

	public Integer getBrokenBlocksYesterday(int id) {
		String sql = "SELECT block_break FROM usertimeline WHERE userid = " + id + " ORDER BY timestamp DESC LIMIT 1";
		ResultSet rs = mysql.query(sql);
		int c = 0;
		int block_break = 0;
		try {
			while (rs.next()) {
				block_break = rs.getInt("block_break");
				c++;
			}
		} catch (SQLException e) {
			return null;
		}
		if (c == 1)
			return block_break;
		else if (c == 0)
			return 0;
		return null;
	}

	@Deprecated
	public void setBrokenBlocks(String player, int amount) {
		String sql = "UPDATE users SET block_break = " + amount + " WHERE name = '" + player + "'";
		mysql.queryUpdate(sql);
	}

	@Deprecated
	public void addBrokenBlocks(String player, int amount) {
		int blocks = getBrokenBlocks(player);
		blocks += amount;
		setBrokenBlocks(player, blocks);
	}

	public String startRegister(String player) {
		Random r = new Random();
		String code = randInt(r, 111, 999) + "-" + randInt(r, 111, 999);
		String sql = "INSERT INTO users (name, code) VALUES ('" + player + "', '" + code + "')";
		mysql.queryUpdate(sql);
		return code;
	}

	public String updateCode(String player) {
		Random r = new Random();
		String code = randInt(r, 111, 999) + "-" + randInt(r, 111, 999);
		String sql = "UPDATE users SET code = '" + code + "' WHERE name = '" + player + "'";
		mysql.queryUpdate(sql);
		return code;
	}

	public String getCode(String player) {
		String sql = "SELECT code FROM " + table + " WHERE name = '" + player + "'";
		ResultSet rs = mysql.query(sql);
		int c = 0;
		String code = "";
		try {
			while (rs.next()) {
				code = rs.getString("code");
				c++;
			}
		} catch (SQLException e) {
			return null;
		}
		if (c == 1)
			return code;
		return null;
	}

	private int randInt(Random r, int min, int max) {
		return r.nextInt((max - min) + 1) + min;
	}

	public ArrayList<EnchantmentItem> getBoughtEnchantmentItems(String player) {
		int id = getID(player);
		if (id >= 0) {
			ArrayList<EnchantmentItem> items = new ArrayList<EnchantmentItem>();
			try {
				String sql = "SELECT s.id, e.bukkitname FROM enchantmentshop AS s JOIN enchantments AS e ON e.id = s.itemid WHERE used = 0 AND userid = ?";
				PreparedStatement stmt = mysql.conn.prepareStatement(sql);
				stmt.setInt(1, id);
				ResultSet rs = stmt.executeQuery();
				while (rs.next()) {
					items.add(new EnchantmentItem(rs.getInt("id"), rs.getString("bukkitname")));
				}
			} catch (SQLException e) {
				return null;
			}
			return items;
		} else {
			return null;
		}
	}

	public void useItem(int itemid) {
		String sql = "UPDATE enchantmentshop SET used = 1 WHERE id = " + itemid;
		mysql.queryUpdate(sql);
	}

	public void resetPassword(int userid) {
		String sql = "UPDATE users SET password = 'NEW' WHERE id = " + userid;
		mysql.queryUpdate(sql);
	}

	public boolean checkDaychange() {
		String sql = "SELECT timestamp FROM serverstatus WHERE status = 'playerupdate'";
		ResultSet rs = mysql.query(sql);
		if (rs != null) {
			Timestamp ts = null;
			int c = 0;
			try {
				while (rs.next()) {
					ts = rs.getTimestamp("timestamp");
					c++;
				}
			} catch (SQLException e) {
				return false;
			}
			if (c != 1)
				return false;
			String s_ts = new SimpleDateFormat("yyyy-MM-dd").format(ts);
			String s_now = getCurrentTime();
			if (!s_ts.equals(s_now))
				return true;
		}
		return false;
	}

	public void updatePlayerinfo() {
		if (checkDaychange()) {
			String sql = "select id, money, block_break, block_place from users;";
			ResultSet rs = mysql.query(sql);
			ArrayList<UserTimeLine> list = new ArrayList<UserTimeLine>();
			try {
				while (rs.next()) {
					int id = rs.getInt("id");
					int money = rs.getInt("money");
					int block_break = rs.getInt("block_break");
					int block_place = rs.getInt("block_place");
					list.add(new UserTimeLine(id, money, block_break, block_place));
				}
			} catch (SQLException e) {
				return;
			}
			for (UserTimeLine utl : list) {
				sql = "INSERT INTO usertimeline (userid, money, block_break, block_place) VALUES (" + utl.id + ", "
						+ utl.money + ", " + utl.block_break + ", " + utl.block_place + ")";
				mysql.queryUpdate(sql);
			}
			setPlayerinfoUpdated();
			PlayerInformator.instance().refreshAllLocalUsers();
		}
	}

	public void setPlayerinfoUpdated() {
		String sql = "update serverstatus set timestamp = NOW() where status = 'playerupdate'";
		mysql.queryUpdate(sql);
	}

	public int getBlockBreakMonthBonus() {
		String sql = "select value from serverstatus where status = 'blockbreakmonthbonus'";
		ResultSet rs = mysql.query(sql);
		int value = -1;
		try {
			if (rs.next()) {
				try {
					value = Integer.parseInt(rs.getString("value"));
				} catch (NumberFormatException e) {
					return -1;
				}
			}
		} catch (SQLException e) {
			return -1;
		}
		return value;
	}

	public int getBlockPlaceMonthBonus() {
		String sql = "select value from serverstatus where status = 'blockplacemonthbonus'";
		ResultSet rs = mysql.query(sql);
		int value = -1;
		try {
			if (rs.next()) {
				try {
					value = Integer.parseInt(rs.getString("value"));
				} catch (NumberFormatException e) {
					return -1;
				}
			}
		} catch (SQLException e) {
			return -1;
		}
		return value;
	}

	public static String getCurrentTime() {
		return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
	}

	public int getID(String player) {
		String sql = "SELECT id FROM " + table + " WHERE name = '" + player + "'";
		ResultSet rs = mysql.query(sql);
		int id = -1;
		int c = 0;
		try {
			while (rs.next()) {
				id = rs.getInt("id");
				c++;
			}
		} catch (SQLException e) {
			return -1;
		}
		if (c == 1)
			return id;
		return -1;
	}

	public MysqlConnector getConnector() {
		return mysql;
	}
}
