package net.Indyuce.mmocore.api.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.bukkit.configuration.ConfigurationSection;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.Indyuce.mmocore.MMOCore;

public class MMOSQL {
	private static Connection connection;
	
	public static boolean testConnection(MySQLConfig config) {		
		try {
		    Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
		    e.printStackTrace();
		    System.err.println("jdbc driver unavailable!");
			return false;
		}
		try {
		    connection = DriverManager.getConnection(config.getConnectionString(), config.getUser(), config.getPassword());
		    MMOCore.log("Successfully connected to MySQL Database!");
		} catch (SQLException e) {
		    e.printStackTrace();
			return false;
		}
		return true;
	}

	public static void stop() {
		try {
	        if(connection != null && !connection.isClosed()) connection.close();
	    } catch(SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	public static void createTables() {
		executeUpdate("CREATE TABLE IF NOT EXISTS mmocore_playerdata (uuid VARCHAR(36),class_points INT(11) DEFAULT 0,skill_points INT(11) DEFAULT 0,attribute_points INT(11) DEFAULT 0,attribute_realloc_points INT(11) DEFAULT 0,level INT(11) DEFAULT 0,experience INT(11) DEFAULT 0,class VARCHAR(20),guild VARCHAR(20),last_login LONG,attributes JSON,professions JSON,quests JSON,waypoints JSON,friends JSON,skills JSON,bound_skills JSON,class_info JSON,PRIMARY KEY (uuid));");
		//executeUpdate("CREATE TABLE IF NOT EXISTS mmocore_guilddata(id varchar(10));");
	}

	public static ResultSet getResult(String sql) {
		try {
		    PreparedStatement stmt = connection.prepareStatement(sql);
			return stmt.executeQuery();
		} catch (SQLException e) {
		    e.printStackTrace();
		    return null;
		}
	}
	
	public static Collection<String> getJSONArray(String json) {
		Collection<String> collection = new ArrayList<String>();
		Gson parser = new Gson();
		
		for(String s : parser.fromJson(json, String[].class))
			collection.add(s);
		
		return collection;
	}
	
	private final Table table;
	private final UUID uuid;
	
	public MMOSQL(Table t, UUID player) {
		table = t;
		uuid = player; 
	}

	public void updateData(String key, Object value) {
		executeUpdate("INSERT INTO " + table + "(uuid, " + key + ") VALUES('" + uuid +
				"', '" + value + "') ON DUPLICATE KEY UPDATE " + key + "='" + value + "';");
	}
	
	public void updateJSONArray(String key, Collection<String> collection) {
		JsonArray json = new JsonArray();
		for(String s : collection)
			json.add(s);
		updateData(key, json.toString());
	}
	
	public void updateJSONObject(String key, Set<Entry<String, Integer>> collection) {
		JsonObject json = new JsonObject();
		for(Entry<String, Integer> entry : collection)
			json.addProperty(entry.getKey(), entry.getValue());
		updateData(key, json.toString());
	}
	
	private static void executeUpdate(String sql) {
		try {
		    PreparedStatement stmt = connection.prepareStatement(sql);
		    stmt.executeUpdate();
		} catch (SQLException e) {
		    e.printStackTrace();
		}
	}
	
	public enum Table {
		PLAYERDATA("mmocore_playerdata"),
		GUILDDATA("mmocore_guilddata");
		
		final String tableName; 
		Table(String tN) {
			tableName = tN;
		}
		
		@Override
		public String toString() {
			return tableName;
		}
	}
	
	public static class MySQLConfig {
		String database, hostname, userid, password, flags;
		int port;
		
		public MySQLConfig(ConfigurationSection config) {
			database = config.getString("database", "minecraft");
			hostname = config.getString("host", "localhost");
			port = config.getInt("port", 3306);
			userid = config.getString("user", "mmolover");
			password = config.getString("pass", "ILoveAria");
			flags = config.getString("flags", "?allowReconnect=true");
		}

		public String getConnectionString() {
			return "jdbc:mysql://" + hostname + ":" + port + "/" + database + flags + "&useSSL=false";
		}
		public String getUser() {
			return userid;
		}
		public String getPassword() {
			return password;
		}
	}
}
