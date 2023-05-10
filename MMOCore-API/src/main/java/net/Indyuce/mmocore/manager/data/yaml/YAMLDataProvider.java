package net.Indyuce.mmocore.manager.data.yaml;

import net.Indyuce.mmocore.manager.data.DataProvider;
import net.Indyuce.mmocore.manager.data.GuildDataManager;
import net.Indyuce.mmocore.manager.data.PlayerDataManager;

public class YAMLDataProvider implements DataProvider {
	private final YAMLPlayerDataManager playerManager = new YAMLPlayerDataManager(this);
	private final YAMLGuildDataManager guildManager = new YAMLGuildDataManager();

	@Override
	public PlayerDataManager getDataManager() {
		return playerManager;
	}

	@Override
	public GuildDataManager getGuildManager() {
		return guildManager;
	}
}
