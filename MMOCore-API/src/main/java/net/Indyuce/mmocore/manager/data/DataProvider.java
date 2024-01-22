package net.Indyuce.mmocore.manager.data;

import net.Indyuce.mmocore.MMOCore;

/**
 * Used to separate MySQL data storage from YAML data storage.
 * <p>
 * There is one data provider per storage mecanism (one for YAML, one for MySQL).
 * A data provider provides corresponding MMOManagers to correctly save and load
 * data
 *
 * @deprecated Not being used anymore, see {@link MMOCore#nativeGuildManager} and {@link MMOCore#playerDataManager}
 */
@Deprecated
public interface DataProvider {

    @Deprecated
    PlayerDataManager getDataManager();

    @Deprecated
    GuildDataManager getGuildManager();
}
