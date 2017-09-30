package de.headshotharp.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.configuration.file.FileConfiguration;

import de.headshotharp.plugin.PlayerInformator;

public class MysqlConnector {
	private String ip = "localhost", dbname = "", username = "", password = "";
	private int port = 3306;
	public Connection conn;

	public MysqlConnector() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		openConnection();
	}

	public MysqlConnector(FileConfiguration cfg) {
		this.ip = cfg.getString("host");
		this.port = cfg.getInt("port");
		this.dbname = cfg.getString("database");
		this.username = cfg.getString("user");
		this.password = cfg.getString("password");
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		openConnection();
	}

	public String getDatabase() {
		return dbname;
	}

	private void openConnection() {
		try {
			conn = DriverManager.getConnection("jdbc:mysql://" + ip + ":" + port + "/" + dbname
					+ "?autoReconnect=true&useSSL=false&zeroDateTimeBehavior=convertToNull&useUnicode=true&characterEncoding=utf-8",
					username, password);
			System.out.println("[PlayerInformator] connection established");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public boolean isConnected() {
		try {
			return conn != null || conn.isValid(1);
		} catch (SQLException e) {
			return false;
		}
	}

	@Deprecated
	public ResultSet query(String sql) {
		try {
			PreparedStatement st = conn.prepareStatement(sql);
			return st.executeQuery();
		} catch (SQLException e) {
			System.out.println("Failed to send query '" + sql + "'.");
			e.printStackTrace();
			System.out.println("Exception caught: No further problems!");
			requestReconnect();
		}
		return null;
	}

	@Deprecated
	public void queryUpdate(String sql) {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(sql);
			st.executeUpdate();
		} catch (SQLException e) {
			System.out.println("Failed to send update '" + sql + "'.");
			e.printStackTrace();
			System.out.println("Exception caught: No further problems!");
			requestReconnect();
		}
	}

	public void requestReconnect() {
		PlayerInformator.instance().getMysql().requestReconnect();
	}

	public void closeConnection() {
		try {
			if (conn != null)
				conn.close();
			System.out.println("connection closed");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void reconnect() {
		if (conn != null) {
			try {
				if (!isConnected()) {
					conn.close();
				}
			} catch (SQLException e) {

			}
		}
		openConnection();
	}
}
