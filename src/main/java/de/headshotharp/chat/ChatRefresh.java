package de.headshotharp.chat;

import java.util.ArrayList;

import de.headshotharp.mysql.Database;
import de.headshotharp.mysql.MysqlConnector;
import de.headshotharp.plugin.PlayerInformator;

public class ChatRefresh implements Runnable {
	private Thread thread;
	private Database local_mysql;
	private int lastid;

	public ChatRefresh() {
		thread = new Thread(this);
		thread.start();
	}

	public void stopRefresh() {
		thread.interrupt();
		local_mysql.getConnector().closeConnection();
	}

	@Override
	public void run() {
		boolean isRunning = true;
		local_mysql = new Database(new MysqlConnector(PlayerInformator.instance().getConfig()));
		lastid = local_mysql.getLastChatId();
		while (isRunning) {
			ArrayList<Chat> chat = local_mysql.getWebChat(lastid);
			if (chat.size() > 0) {
				for (Chat c : chat) {
					PlayerInformator.printChat(c);
				}
				lastid = chat.get(chat.size() - 1).getId();
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				isRunning = false;
				break;
			}
		}
		System.out.println("[PlayerInformator] Chat refresh interrupted");
	}
}
