package net.Indyuce.mmocore.api.data;

import net.Indyuce.mmocore.manager.data.PlayerDataManager;
import net.Indyuce.mmocore.manager.data.YAMLPlayerDataManager;
import net.Indyuce.mmocore.manager.social.GuildManager;

public class YAMLDataProvider implements DataProvider {
	
	@Override
	public PlayerDataManager provideDataManager() {
		return new YAMLPlayerDataManager();
	}

	@Override
	public GuildManager provideGuildManager() {
		return new GuildManager();
	}
}
