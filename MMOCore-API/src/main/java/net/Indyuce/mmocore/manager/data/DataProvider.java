package net.Indyuce.mmocore.manager.data;

/**
 * Used to separate MySQL data storage from YAML data storage.
 * <p>
 * There is one data provider per storage mecanism (one for YAML, one for MySQL).
 * A data provider provides corresponding MMOManagers to correctly save and load
 * data
 */
public interface DataProvider {

    PlayerDataManager getDataManager();

    GuildDataManager getGuildManager();
}
