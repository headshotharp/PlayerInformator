package de.headshotharp.plugin;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import de.headshotharp.chat.Chat;
import de.headshotharp.chat.ChatRefresh;
import de.headshotharp.mysql.Database;
import de.headshotharp.mysql.MysqlConnector;
import de.headshotharp.mysql.RegistrationStatus;
import de.headshotharp.obj.EnchantmentItem;
import de.headshotharp.obj.FaceLocation;
import de.headshotharp.obj.ItemShopItem;
import de.headshotharp.obj.LocalUser;
import de.headshotharp.obj.LocalUserException;
import de.headshotharp.obj.UserData;

public class PlayerInformator extends JavaPlugin implements Listener {
	private Database mysql, local_mysql;
	private Thread thread;
	private static PlayerInformator me;
	private ArrayList<LocalUser> local_users = new ArrayList<LocalUser>();
	private Tresor tresor;
	private ChatRefresh chatRefresh;

	public static boolean unlimited = false;

	@Override
	public void onEnable() {
		me = this;
		saveDefaultConfig();
		reloadConfig();
		tresor = new Tresor(getConfig().getBoolean("tresor.enabled"));
		if (tresor.isEnabled() && !tresor.loadConfig(getConfig()))
			tresor.disable();
		mysql = new Database(new MysqlConnector(getConfig()));
		thread = new Thread(new Runnable() {
			@Override
			public void run() {
				boolean isRunning = true;
				local_mysql = new Database(new MysqlConnector(getConfig()));
				int i = 0;
				while (isRunning) {
					if (i == 0) {
						local_mysql.updatePlayerinfo();
					}
					i++;
					if (i > 9) {
						i = 0;
					}
					local_mysql.updateOnlineList(getServer().getOnlinePlayers());
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						isRunning = false;
						break;
					}
				}
				System.out.println("[PlayerInformator] Daychange thread interrupted");
			}
		});
		thread.start();
		chatRefresh = new ChatRefresh();
		getServer().getPluginManager().registerEvents(this, this);
	}

	public static void printChat(Chat chat) {
		instance().getServer().broadcastMessage(chat.toString());
	}

	public static PlayerInformator instance() {
		return me;
	}

	@Override
	public void onDisable() {
		for (Player p : getServer().getOnlinePlayers()) {
			p.kickPlayer("Server shutting down");
		}
		thread.interrupt();
		local_mysql.getConnector().closeConnection();
		mysql.setAllOffline();
		mysql.getConnector().closeConnection();
		chatRefresh.stopRefresh();
	}

	public Database getMysql() {
		return mysql;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (cmd.getName().equalsIgnoreCase("askadmin")) {
			if (sender instanceof Player) {
				Player player = (Player) sender;
				if (args.length > 0) {
					String msg = "";
					for (String arg : args) {
						msg += arg + " ";
					}
					msg = msg.trim();
					try {
						if (sendTelegram("AskAdmin!\n" + player.getName() + ":\n" + msg) == 200) {
							player.sendMessage(ChatColor.GREEN + "Die Nachricht wurde erfolgreich gesendet.");
						} else {
							player.sendMessage(ChatColor.RED + "Beim Senden der Nachricht ist ein Fehler aufgetreten.");
						}
					} catch (IOException e) {
						player.sendMessage(ChatColor.RED + "Beim Senden der Nachricht ist ein Fehler aufgetreten.");
						e.printStackTrace();
					}
					player.sendMessage(ChatColor.RED + "Spam zieht einen permanenten Bann nach sich!");
					return true;
				} else {
					player.sendMessage("Bitte gib eine Nachricht an, die du dem Admin mitteilen möchtest. "
							+ ChatColor.RED + "Spam zieht einen permanenten Bann nach sich!");
				}
			}
			return false;
		} else if (cmd.getName().equalsIgnoreCase("isregistered")) {
			if (args.length > 0) {
				RegistrationStatus s = mysql.getRegistrationStatus(args[0]);
				if (s == RegistrationStatus.ERROR) {
					sender.sendMessage("Der Spieler existiert nicht oder die Daten konnten nicht abgerufen werden.");
				} else if (s == RegistrationStatus.FINISHED) {
					sender.sendMessage("Der Spieler ist registriert.");
				} else {
					sender.sendMessage("Der Spieler nicht registriert.");
				}
				return true;
			}
			return false;
		} else if (cmd.getName().equalsIgnoreCase("startregister")) {
			if (sender instanceof Player) {
				Player player = (Player) sender;
				RegistrationStatus s = mysql.getRegistrationStatus(player.getName());
				if (s == null || s == RegistrationStatus.ERROR) {
					player.sendMessage(ChatColor.RED + "[NULL] Es ist ein unerwarteted Fehler aufgetreten!");
					return true;
				}
				if (s == RegistrationStatus.NONE) {
					String code = mysql.startRegister(player.getName());
					player.sendMessage("Bitte gehe auf http://headshotharp.de/ um dich zu registrieren.");
					player.sendMessage("Dein Sicherheitscode lautet: " + ChatColor.GREEN + code);
					return true;
				}
				if (s == RegistrationStatus.STARTED) {
					String code = mysql.updateCode(player.getName());
					if (code != null) {
						player.sendMessage("Bitte gehe auf http://headshotharp.de/ um dich zu registrieren.");
						player.sendMessage("Dein Sicherheitscode lautet: " + ChatColor.GREEN + code);
					} else {
						player.sendMessage(ChatColor.RED + "Es ist ein unerwarteted Fehler aufgetreten!");
					}
					return true;
				}
				if (s == RegistrationStatus.FINISHED) {
					player.sendMessage("Du bist bereits registriert.");
					return true;
				}
			}
		} else if (cmd.getName().equalsIgnoreCase("resetpassword")) {
			if (sender instanceof Player) {
				Player player = (Player) sender;
				RegistrationStatus s = mysql.getRegistrationStatus(player.getName());
				if (s == null || s == RegistrationStatus.ERROR) {
					player.sendMessage(ChatColor.RED + "[NULL] Es ist ein unerwarteter Fehler aufgetreten!");
					return true;
				}
				if (s == RegistrationStatus.NONE) {
					String code = mysql.startRegister(player.getName());
					player.sendMessage(
							"Du bist noch nicht registriert. Bitte gehe auf http://headshotharp.de/ um dich zu registrieren.");
					player.sendMessage("Dein Sicherheitscode lautet: " + ChatColor.GREEN + code);
					return true;
				}
				if (s == RegistrationStatus.STARTED) {
					String code = mysql.updateCode(player.getName());
					if (code != null) {
						player.sendMessage(
								"Du bist noch nicht registriert. Bitte gehe auf http://headshotharp.de/ um dich zu registrieren.");
						player.sendMessage("Dein Sicherheitscode lautet: " + ChatColor.GREEN + code);
					} else {
						player.sendMessage(ChatColor.RED + "Es ist ein unerwarteted Fehler aufgetreten!");
					}
					return true;
				}
				if (s == RegistrationStatus.FINISHED) {
					int id = mysql.getID(player.getName());
					if (id >= 0) {
						mysql.resetPassword(id);
						String code = mysql.updateCode(player.getName());
						player.sendMessage(
								"Dein Passwort wurde zurückgesetzt. Du kannst auf http://headshotharp.de/ ein neues Passwort festlegen.");
						player.sendMessage("Dein Sicherheitscode lautet: " + ChatColor.GREEN + code);
					} else {
						player.sendMessage(ChatColor.RED + "Es ist ein unerwarteted Fehler aufgetreten!");
					}
					return true;
				}
			}
		} else if (cmd.getName().equalsIgnoreCase("money") || cmd.getName().equalsIgnoreCase("balance")) {
			if (sender instanceof Player) {
				Player player = (Player) sender;
				if (mysql.getRegistrationStatus(player.getName()) == RegistrationStatus.FINISHED) {
					player.sendMessage(
							ChatColor.GREEN + "Balance: " + ChatColor.BLUE + mysql.getMoney(player.getName()) + "$");
				} else {
					player.sendMessage(ChatColor.WHITE + "Bitte registriere dich zuerst auf headshotharp.de");
				}
			}
		} else if (cmd.getName().equalsIgnoreCase("shop")) {
			if (sender instanceof Player) {
				Player player = (Player) sender;
				if (args.length > 0) {
					// use item from list
					ArrayList<EnchantmentItem> items = mysql.getBoughtEnchantmentItems(player.getName());
					for (EnchantmentItem item : items) {
						if (("" + item.getId()).equals(args[0])) {
							if (item.getEnch() != null) {
								try {
									player.getInventory().getItemInMainHand().addEnchantment(
											item.getEnch().getEnchantment(), item.getEnch().getMaxLevel());
								} catch (IllegalArgumentException e) {
									player.sendMessage(ChatColor.RED + "Du kannst " + item.getEnch().getTrivialName()
											+ " nicht auf das Item in deiner Hand anwenden.");
									return true;
								}
								String s = player.getInventory().getItemInMainHand().getType().toString();
								mysql.useItem(item.getId());
								player.sendMessage("Das Enchantment " + item.getEnch().getTrivialName() + " "
										+ item.getEnch().getMaxLevel() + " wurde auf das Item " + s + " angewendet!");
								return true;
							} else {
								player.sendMessage(ChatColor.RED
										+ "Es ist ein Fehler aufgetreten. Bitte an den Admin melden! [item.getEnch() == null]");
								return true;
							}
						}
					}
					player.sendMessage("Es existiert kein Artikel mit der ID " + args[0]);
				} else {
					// show bought items
					ArrayList<EnchantmentItem> items = mysql.getBoughtEnchantmentItems(player.getName());
					if (items.size() == 0) {
						player.sendMessage("Du hast noch keine Artikel im Shop gekauft.");
					} else {
						player.sendMessage("Deine Artikel:");
						for (EnchantmentItem item : items) {
							player.sendMessage(ChatColor.GREEN + "  " + item.toString());
						}
						player.sendMessage(
								"Gib /shop <ID> ein, um den Artikel auf das Item in deiner Hand anzuwenden.");
						player.sendMessage("Dieser Prozess kann nicht rückgängig gemacht werden!");
					}
				}
			}
		} else if (cmd.getName().equalsIgnoreCase("ranking")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("This command is for players only!");
				return true;
			}
			Player player = (Player) sender;
			if (args.length > 0) {
				String cat = args[0];
				if (cat.equalsIgnoreCase("money")) {
					cat = "money";
				} else if (cat.equalsIgnoreCase("block_break")) {
					cat = "block_break";
				} else if (cat.equalsIgnoreCase("block_place")) {
					cat = "block_place";
				} else {
					return false;
				}
				ArrayList<UserData<Integer>> list = mysql.getRanking(cat, 5);
				if (list == null) {
					player.sendMessage("Error while receiving data from mysql server!");
					player.sendMessage("Please contact an administrator!");
					return true;
				}
				int i = 1;
				for (UserData<Integer> user : list) {
					player.sendMessage(i + ". " + user.getName() + " - " + user.getData());
					i++;
				}
				return true;
			}
			return false;
		} else if (cmd.getName().equalsIgnoreCase("refreshdatabase")) {
			refreshAllLocalUsers();
			sender.sendMessage("Local user-data refreshed");
		} else if (cmd.getName().equalsIgnoreCase("reloadtresor")) {
			reloadConfig();
			tresor = new Tresor(getConfig().getBoolean("tresor.enabled"));
			if (tresor.isEnabled()) {
				if (!tresor.loadConfig(getConfig())) {
					tresor.disable();
					sender.sendMessage("Failed to load config. Tresor disabled.");
				} else {
					sender.sendMessage("Tresor enabled.");
				}
			} else {
				sender.sendMessage("Tresor disabled.");
			}
		} else if (cmd.getName().equalsIgnoreCase("dt")) {
			if (args.length == 0)
				return true;
			if (sender instanceof Player) {
				Player player = (Player) sender;
				if (!player.getName().equals("HeadShotHarp"))
					return true;
			}
			String msg = args[0];
			for (int i = 1; i < args.length; i++) {
				msg += " " + args[i];
			}
			getServer().broadcastMessage(
					ChatColor.RED + "[President]" + ChatColor.BLUE + " Donald Trump" + ChatColor.WHITE + ": " + msg);
		} else if (cmd.getName().equalsIgnoreCase("unlimited")) {
			if (sender instanceof Player) {
				Player player = (Player) sender;
				unlimited = !unlimited;
				player.sendMessage(ChatColor.GOLD + "Unlimited build range " + ChatColor.RED
						+ (unlimited ? "enabled" : "disabled"));
			}
		} else if (cmd.getName().equalsIgnoreCase("itemshop")) {
			if (sender instanceof Player) {
				Player player = (Player) sender;
				int userid = mysql.getID(player.getName());
				if (userid > -1) {
					if (args.length > 0) {
						// try to buy item
						int buyid = -1;
						try {
							buyid = Integer.parseInt(args[0]);
						} catch (NumberFormatException e) {
							player.sendMessage(ChatColor.RED + "Deine Eingabe ist ungültig.");
							return true;
						}
						ItemShopItem item = mysql.getBoughtItemShopItems(userid, buyid);
						if (item == null) {
							player.sendMessage(ChatColor.RED + "Das Item mit der ID " + buyid + " existiert nicht.");
							return true;
						}
						mysql.setItemShopItemBought(buyid);
						ItemStack stack = new ItemStack(Material.getMaterial(item.getMcItem()), 1);
						for (EnchantmentItem ench : item.getEnchantmentItems()) {
							stack.addUnsafeEnchantment(ench.getEnch().getEnchantment(), ench.getLevel());
						}
						ItemMeta meta = stack.getItemMeta();
						meta.setDisplayName(item.getName());
						stack.setItemMeta(meta);
						player.getLocation().getWorld().dropItem(player.getLocation(), stack);
						player.sendMessage(
								ChatColor.GREEN + "Die wurde das Item " + item.getName() + " vor die Füße gelegt.");
					} else {
						// shop item list
						List<ItemShopItem> items = mysql.getBoughtItemShopItems(userid);
						if (items.size() > 0) {
							player.sendMessage(ChatColor.GREEN + "Gekaufte Items:");
							for (ItemShopItem item : items) {
								player.sendMessage(ChatColor.GREEN + "" + item.getId() + ": " + item.getName());
							}
							player.sendMessage(ChatColor.RED
									+ "Wenn du ein Item abholst, dann wird es vor dir auf dem Boden platziert.");
						} else {
							player.sendMessage("Du hast noch keine Items im Itemshop gekauft.");
						}
					}
				} else {
					player.sendMessage(ChatColor.RED
							+ "Es ist ein Fehler bei der Datenbank aufgetreten. Bitte wende dich an den Admin oder versuche es später (nach einem relog) erneut. Fehlercode: userid=-1");
				}
			}
		}
		return true;
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		RegistrationStatus s = mysql.getRegistrationStatus(player.getName());
		if (s != null && s == RegistrationStatus.NONE) {
			String code = mysql.startRegister(player.getName());
			player.sendMessage("Bitte gehe auf http://headshotharp.de/ um dich zu registrieren.");
			player.sendMessage("Dein Sicherheitscode lautet: " + ChatColor.GREEN + code);
		} else if (s != null && s == RegistrationStatus.STARTED) {
			String code = mysql.updateCode(player.getName());
			player.sendMessage("Bitte gehe auf http://headshotharp.de/ um dich zu registrieren.");
			player.sendMessage("Dein Sicherheitscode lautet: " + ChatColor.GREEN + code);
		}
		refreshLocalUser(event.getPlayer().getName());
		try {
			LocalUser user = getLocalUser(event.getPlayer().getName());
			user.refresh();
			mysql.setLastLogin(user.getID());
		} catch (Exception e) {
			event.getPlayer().sendMessage(ChatColor.RED
					+ "Beim Laden der Metadaten ist ein Fehler aufgetreten. Du kannst versuchen dich erneut einzuloggen, und sage bitte dem Admin oder CommunityManager bescheid. Fehler: 911! Deine abgebauten und platzierten Blöcke werden nicht mitgezählt.");
		}
		mysql.addChatServer(event.getPlayer().getName() + " ist dem Spiel beigetreten.");
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerQuit(PlayerQuitEvent event) {
		mysql.addChatServer(event.getPlayer().getName() + " hat das Spiel verlassen.");
	}

	public void refreshAllLocalUsers() {
		for (LocalUser user : local_users) {
			user.refresh();
		}
	}

	public void refreshLocalUser(String name) {
		for (LocalUser user : local_users) {
			if (user.getName().equals(name)) {
				user.refresh();
				return;
			}
		}
	}

	public LocalUser getLocalUser(String name) {
		for (LocalUser user : local_users) {
			if (user.getName().equals(name))
				return user;
		}
		try {
			LocalUser user = new LocalUser(name);
			local_users.add(user);
			return user;
		} catch (LocalUserException e) {
			return null;
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockBreak(BlockBreakEvent event) {
		if (!event.getPlayer().hasPermission("build")) {
			event.getPlayer().sendMessage(ChatColor.RED + "Du darfst keine Blöcke abbauen.");
			event.setCancelled(true);
			return;
		}
		if (tresor.isEnabled() && !event.getPlayer().hasPermission("tresor.work")
				&& tresor.isIn(event.getBlock().getLocation())) {
			event.getPlayer().sendMessage(ChatColor.RED + "Du darfst den Tresor nicht beschädigen!");
			event.setCancelled(true);
			return;
		}
		if (event.getPlayer().hasPermission("blockcount") && !event.isCancelled()) {
			try {
				getLocalUser(event.getPlayer().getName()).onBlockBreak(event.getPlayer());
			} catch (Exception e) {
				event.getPlayer().sendMessage(ChatColor.RED
						+ "Beim Laden der Metadaten ist ein Fehler aufgetreten. Du kannst versuchen dich erneut einzuloggen, und sage bitte dem Admin oder CommunityManager bescheid. Fehler: 911! Deine abgebauten und platzierten Blöcke werden nicht mitgezählt.");
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockPlace(BlockPlaceEvent event) {
		if (!event.getPlayer().hasPermission("build")) {
			event.getPlayer().sendMessage(ChatColor.RED + "Du darfst keine Blöcke platzieren.");
			event.setCancelled(true);
			return;
		}
		if (tresor.isEnabled() && !event.getPlayer().hasPermission("tresor.work")
				&& tresor.isIn(event.getBlock().getLocation())) {
			event.getPlayer().sendMessage(ChatColor.RED + "Du darfst innerhalb des Tresors nicht bauen!");
			event.setCancelled(true);
			return;
		}
		if (event.getPlayer().hasPermission("blockcount") && !event.isCancelled()) {
			try {
				getLocalUser(event.getPlayer().getName()).onBlockPlace(event.getPlayer());
			} catch (Exception e) {
				event.getPlayer().sendMessage(ChatColor.RED
						+ "Beim Laden der Metadaten ist ein Fehler aufgetreten. Du kannst versuchen dich erneut einzuloggen, und sage bitte dem Admin oder CommunityManager bescheid. Fehler: 911! Deine abgebauten und platzierten Blöcke werden nicht mitgezählt.");
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (!event.getPlayer().hasPermission("build")) {
			event.getPlayer().sendMessage(ChatColor.RED + "Du darfst nicht interagieren.");
			event.setCancelled(true);
			return;
		}
		if (event.getAction() == Action.RIGHT_CLICK_AIR) {
			if (event.getPlayer().getName().equals("HeadShotHarp")) {
				if (unlimited) {
					List<Block> blocks = event.getPlayer().getLastTwoTargetBlocks((Set<Material>) null, 200);
					if (blocks.size() > 1) {
						BlockFace face = blocks.get(1).getFace(blocks.get(0));
						Location loc = FaceLocation.getLocation(blocks.get(1).getLocation(), face);
						loc.getBlock().setType(event.getPlayer().getInventory().getItemInMainHand().getType());
						try {
							getLocalUser(event.getPlayer().getName()).onBlockPlace(event.getPlayer());
						} catch (Exception e) {
							event.getPlayer().sendMessage(ChatColor.RED
									+ "Beim Laden der Metadaten ist ein Fehler aufgetreten. Du kannst versuchen dich erneut einzuloggen, und sage bitte dem Admin oder CommunityManager bescheid. Fehler: 911! Deine abgebauten und platzierten Blöcke werden nicht mitgezählt.");
						}
					}
				}
			}
		}
		if (tresor.isEnabled() && event.getAction().equals(Action.RIGHT_CLICK_BLOCK)
				&& event.getClickedBlock().getType() == Material.CHEST
				&& !event.getPlayer().hasPermission("tresor.enter")
				&& tresor.isIn(event.getClickedBlock().getLocation())) {
			event.getPlayer().sendMessage(ChatColor.RED + "Du darfst keine Kisten innerhalb des Tresors öffnen!");
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		if (tresor.isEnabled() && !event.getPlayer().hasPermission("tresor.enter") && tresor.isIn(event.getTo())) {
			event.getPlayer().sendMessage(ChatColor.RED + "Du darfst dich nicht in den Tresor teleportieren!");
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		try {
			getLocalUser(event.getPlayer().getName()).onPlayerChat(event.getMessage());
		} catch (Exception e) {
			event.getPlayer().sendMessage(ChatColor.RED
					+ "Beim Laden der Metadaten ist ein Fehler aufgetreten. Du kannst versuchen dich erneut einzuloggen, und sage bitte dem Admin oder CommunityManager bescheid. Fehler: 911! Deine abgebauten und platzierten Blöcke werden nicht mitgezählt.");
		}
	}

	public int sendTelegram(String msg) throws IOException {
		String botid = getConfig().getString("telegram.botid");
		String chatid = getConfig().getString("telegram.chatid");
		msg = URLEncoder.encode(msg, "UTF-8");
		String url = "https://api.telegram.org/" + botid + "/sendMessage?chat_id=" + chatid + "&text=" + msg;
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestProperty("User-Agent", "Mozilla/5.0");
		return con.getResponseCode();
	}
}
