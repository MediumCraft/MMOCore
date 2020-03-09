package net.Indyuce.mmocore.api.data;

import net.Indyuce.mmocore.manager.data.GuildDataManager;
import net.Indyuce.mmocore.manager.data.PlayerDataManager;

public interface DataProvider {

	/*
	 * used to separate MySQL data storage from YAML data storage. there is one
	 * dataProvider per storage mecanism (one for YAML, one for MySQL). a
	 * dataProvider provides corresponding mmoManagers to correctly save and load
	 * data
	 */

	PlayerDataManager getDataManager();
	GuildDataManager getGuildManager();
}
