package net.Indyuce.mmocore.manager.data.mysql;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.bukkit.configuration.ConfigurationSection;

import com.github.jasync.sql.db.QueryResult;
import com.github.jasync.sql.db.ResultSet;
import com.github.jasync.sql.db.mysql.MySQLConnection;
import com.github.jasync.sql.db.mysql.MySQLConnectionBuilder;
import com.github.jasync.sql.db.pool.ConnectionPool;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.manager.data.DataProvider;
import net.Indyuce.mmocore.manager.data.GuildDataManager;
import net.Indyuce.mmocore.manager.data.PlayerDataManager;
import net.Indyuce.mmocore.manager.data.yaml.YAMLGuildDataManager;

public class MySQLDataProvider implements DataProvider {
	private final MySQLPlayerDataManager playerManager = new MySQLPlayerDataManager(this);
	private final YAMLGuildDataManager guildManager = new YAMLGuildDataManager();
	private final MySQLConfig config;
	
	private ConnectionPool<MySQLConnection> connection;

	public MySQLDataProvider() {
		config = new MySQLConfig(MMOCore.plugin.getConfig().getConfigurationSection("mysql"));
		connection = MySQLConnectionBuilder.createConnectionPool(config.getConnectionString());

		executeUpdate("CREATE TABLE IF NOT EXISTS mmocore_playerdata (uuid VARCHAR(36),class_points INT(11) DEFAULT 0,skill_points INT(11) DEFAULT 0,attribute_points INT(11) DEFAULT 0,attribute_realloc_points INT(11) DEFAULT 0,level INT(11) DEFAULT 0,experience INT(11) DEFAULT 0,class VARCHAR(20),guild VARCHAR(20),last_login LONG,attributes JSON,professions JSON,quests JSON,waypoints JSON,friends JSON,skills JSON,bound_skills JSON,class_info JSON,PRIMARY KEY (uuid));");
	}

	public ResultSet getResult(String sql) {
		try {
			CompletableFuture<QueryResult> future = connection.sendPreparedStatement(sql);
			return future.get().getRows();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void executeUpdate(String sql) {
		try {
			connection.sendPreparedStatement(sql).get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}

	@Override
	public PlayerDataManager getDataManager() {
		return playerManager;
	}

	@Override
	public GuildDataManager getGuildManager() {
		return guildManager;
	}

	public class MySQLConfig {
		private final String db, host, user, pass;
		private final int port;

		public MySQLConfig(ConfigurationSection config) {
			db = config.getString("database", "minecraft");
			host = config.getString("host", "localhost");
			port = config.getInt("port", 3306);
			user = config.getString("user", "mmolover");
			pass = config.getString("pass", "ILoveAria");
		}

		public String getConnectionString() {
			StringBuilder sb = new StringBuilder("jdbc:mysql://");
			sb.append(host).append(":").append(port).append("/").append(db)
			.append("?user=").append(user).append("&password=").append(pass);
			return sb.toString();
		}
	}
}
