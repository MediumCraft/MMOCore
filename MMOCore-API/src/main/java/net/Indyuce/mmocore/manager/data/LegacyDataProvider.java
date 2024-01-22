package net.Indyuce.mmocore.manager.data;


import net.Indyuce.mmocore.MMOCore;

@Deprecated
public class LegacyDataProvider implements DataProvider {

    @Override
    public PlayerDataManager getDataManager() {
        return MMOCore.plugin.playerDataManager;
    }

    @Override
    public GuildDataManager getGuildManager() {
        return MMOCore.plugin.nativeGuildManager;
    }
}
