package de.headshotharp.obj;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import de.headshotharp.mysql.Database;
import de.headshotharp.plugin.PlayerInformator;

public class LocalUser {
	private int id;
	private String name;
	private int block_break, block_place;

	public LocalUser(String name) throws LocalUserException {
		id = mysql().getID(name);
		if (id == -1) {
			mysql().startRegister(name);
			id = mysql().getID(name);
			if (id == -1) {
				this.name = "ERROR";
				throw new LocalUserException();
			}
		}
		this.name = name;
		if (!refresh())
			throw new LocalUserException();
	}

	public int getID() {
		return id;
	}

	public String getName() {
		return name;
	}

	public int getBlock_break() {
		return block_break;
	}

	public int getBlock_place() {
		return block_place;
	}

	public boolean refresh() {
		try {
			block_break = mysql().getBrokenBlocks(name) - mysql().getBrokenBlocksYesterday(id);
			block_place = mysql().getPlacedBlocks(name) - mysql().getPlacedBlocksYesterday(id);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public void onPlayerChat(String msg) {
		mysql().addChatPlayer(id, msg);
	}

	public static String plainText(String msg) {
		return msg.replace("\"", "\\\"");
	}

	public void onBlockBreak(Player player) {
		block_break++;
		mysql().incBrokenBlocks(name);
		if (block_break % 1000 == 0) {
			int x = block_break / 1000;
			if (x > 50)
				return;
			if (x <= 20 || x % 5 == 0) {
				player.sendMessage(ChatColor.GOLD + "Glückwunsch du hast heute " + block_break + " Blöcke abgebaut!");
			} else
				return;
			int bonus = getMoneyBonus(block_break);
			player.sendMessage(ChatColor.GOLD + "Als Bonus erhältst du " + ChatColor.GREEN + bonus + "R$");
			int serverbonus = mysql().getBlockBreakMonthBonus();
			if (serverbonus > 0) {
				serverbonus = (int) (bonus * (serverbonus / 100.0f));
				player.sendMessage(ChatColor.GOLD + "Als Serverbonus erhältst du zusätzlich " + ChatColor.GREEN
						+ serverbonus + "R$");
				bonus += serverbonus;
			}
			mysql().addMoney(name, bonus);
		}
	}

	public void onBlockPlace(Player player) {
		block_place++;
		mysql().incPlacedBlocks(name);
		if (block_place % 1000 == 0) {
			int x = block_place / 1000;
			if (x > 50)
				return;
			if (x <= 20 || x % 5 == 0) {
				player.sendMessage(ChatColor.GOLD + "Glückwunsch du hast heute " + block_place + " Blöcke platziert!");
			} else
				return;
			int bonus = getMoneyBonus(block_place);
			player.sendMessage(ChatColor.GOLD + "Als Bonus erhältst du " + ChatColor.GREEN + bonus + "R$");
			int serverbonus = mysql().getBlockPlaceMonthBonus();
			if (serverbonus > 0) {
				serverbonus = (int) (bonus * (serverbonus / 100.0f));
				player.sendMessage(ChatColor.GOLD + "Als Serverbonus erhältst du zusätzlich " + ChatColor.GREEN
						+ serverbonus + "R$");
			}
			mysql().addMoney(name, bonus);
		}
	}

	public static int getMoneyBonus(int blocks) {
		int x = blocks / 1000;
		if (x > 50)
			return 0;
		if (x <= 20)
			return (int) ((2 + (x * x) / 10.0f) * 100);
		else if (x % 5 == 0)
			return 5000 * (x / 5) / 5;
		return 0;
	}

	private static Database mysql() {
		return PlayerInformator.instance().getMysql();
	}
}
