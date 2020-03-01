package net.Indyuce.mmocore.api.data;

import net.Indyuce.mmocore.manager.data.PlayerDataManager;
import net.Indyuce.mmocore.manager.social.GuildManager;

public interface DataProvider {

	/*
	 * used to separate MySQL data storage from YAML data storage. there is one
	 * dataProvider per storage mecanism (one for YAML, one for MySQL). a
	 * dataProvider provides corresponding mmoManagers to correctly save and load
	 * data
	 */

	PlayerDataManager provideDataManager();

	GuildManager provideGuildManager();
}
