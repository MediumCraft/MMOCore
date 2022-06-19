package net.Indyuce.mmocore.manager.data.mysql;

import io.lumine.mythic.lib.sql.MMODataSource;
import net.Indyuce.mmocore.manager.data.DataProvider;
import net.Indyuce.mmocore.manager.data.GuildDataManager;
import net.Indyuce.mmocore.manager.data.PlayerDataManager;
import net.Indyuce.mmocore.manager.data.yaml.YAMLGuildDataManager;
import org.bukkit.configuration.file.FileConfiguration;

public class MySQLDataProvider extends MMODataSource implements DataProvider {
	private final MySQLPlayerDataManager playerManager = new MySQLPlayerDataManager(this);
	private final YAMLGuildDataManager guildManager = new YAMLGuildDataManager();

	public MySQLDataProvider(FileConfiguration config) {
		this.setup(config);
	}

	@Override
	public void load() {


			/*TODO
			also move Debug function to mysql data provider

			String tableName = getPoolName();
getResultAsync("SELECT * FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = '" +getPoolName()+ "' AND TABLE_NAME = 'mmocore_playerdata' AND COLUMN_NAME = 'times_claimed'",
		result -> {
			if (!result.next())
				executeUpdateAsync()
		}):
		executeUpdateAsync();


		if (!provider.prepareStatement("SELECT * FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = '" + provider.getDatabase()
				+ "' AND TABLE_NAME = '" + provider.getBountyDataTable() + "' AND COLUMN_NAME = 'last_updated'").executeQuery().next())
			provider.prepareStatement("ALTER TABLE " + provider.getBountyDataTable() + " ADD COLUMN last_updated BIGINT").execute();*/

		executeUpdateAsync(
			"CREATE TABLE IF NOT EXISTS mmocore_playerdata(uuid VARCHAR(36),class_points "
			+ "INT(11) DEFAULT 0,skill_points INT(11) DEFAULT 0,attribute_points INT(11) "
			+ "DEFAULT 0,attribute_realloc_points INT(11) DEFAULT 0,level INT(11) DEFAULT 1,"
			+ "experience INT(11) DEFAULT 0,class VARCHAR(20),guild VARCHAR(20),last_login LONG,"
			+ "attributes LONGTEXT,professions LONGTEXT,times_claimed LONGTEXT,quests LONGTEXT,"
			+ "waypoints LONGTEXT,friends LONGTEXT,skills LONGTEXT,bound_skills LONGTEXT,"
			+ "class_info LONGTEXT, PRIMARY KEY (uuid));");
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
