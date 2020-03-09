package net.Indyuce.mmocore.api.data;

import net.Indyuce.mmocore.api.util.MMOSQL;
import net.Indyuce.mmocore.manager.data.GuildDataManager;
import net.Indyuce.mmocore.manager.data.MySQLPlayerDataManager;
import net.Indyuce.mmocore.manager.data.PlayerDataManager;
import net.Indyuce.mmocore.manager.data.YAMLGuildDataManager;

public class MySQLDataProvider implements DataProvider {
	private final MySQLPlayerDataManager playerManager;
	private final YAMLGuildDataManager guildManager;
	
	public MySQLDataProvider() {
		playerManager = new MySQLPlayerDataManager();
		guildManager = new YAMLGuildDataManager();
		
		MMOSQL.createTables();
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
