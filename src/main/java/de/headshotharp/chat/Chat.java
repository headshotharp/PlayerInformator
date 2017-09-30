package de.headshotharp.chat;

import org.bukkit.ChatColor;

public class Chat {
	private int id;
	private String name;
	private String msg;

	public Chat(int id, String name, String msg) {
		this.id = id;
		this.name = name;
		this.msg = msg;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getMsg() {
		return msg;
	}

	@Override
	public String toString() {
		return ChatColor.YELLOW + "[WEB] " + ChatColor.GRAY + name + ChatColor.WHITE + ": " + msg;
	}
}
