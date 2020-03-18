package net.Indyuce.mmocore.api.data;

import net.Indyuce.mmocore.manager.data.GuildDataManager;
import net.Indyuce.mmocore.manager.data.PlayerDataManager;
import net.Indyuce.mmocore.manager.data.YAMLGuildDataManager;
import net.Indyuce.mmocore.manager.data.YAMLPlayerDataManager;

public class YAMLDataProvider implements DataProvider {
	private final YAMLPlayerDataManager playerManager;
	private final YAMLGuildDataManager guildManager;

	public YAMLDataProvider() {
		playerManager = new YAMLPlayerDataManager();
		guildManager = new YAMLGuildDataManager();
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
