package net.Indyuce.mmocore.manager.data.mysql;

import net.Indyuce.mmocore.manager.data.DataProvider;
import net.Indyuce.mmocore.manager.data.GuildDataManager;
import net.Indyuce.mmocore.manager.data.PlayerDataManager;
import net.Indyuce.mmocore.manager.data.yaml.YAMLGuildDataManager;
import net.mmogroup.mmolib.sql.MMODataSource;

public class MySQLDataProvider extends MMODataSource implements DataProvider {
	private final MySQLPlayerDataManager playerManager = new MySQLPlayerDataManager(this);
	private final YAMLGuildDataManager guildManager = new YAMLGuildDataManager();
	
	@Override
	public void load() {
		executeUpdateAsync("CREATE TABLE IF NOT EXISTS mmocore_playerdata"
			+ "(uuid VARCHAR(36),class_points INT(11) DEFAULT 0,skill_points INT(11)"
			+ "DEFAULT 0,attribute_points INT(11) DEFAULT 0,attribute_realloc_points INT(11)"
			+ "DEFAULT 0,level INT(11) DEFAULT 1,experience INT(11) DEFAULT 0,class VARCHAR(20),"
			+ "guild VARCHAR(20),last_login LONG,attributes JSON,professions JSON,quests JSON,waypoints"
			+ "JSON,friends JSON,skills JSON,bound_skills JSON,class_info JSON,PRIMARY KEY (uuid));");
	}

	@Override
	public PlayerDataManager getDataManager() {
		return playerManager;
	}

	@Override
	public GuildDataManager getGuildManager() {
		return guildManager;
	}
}
