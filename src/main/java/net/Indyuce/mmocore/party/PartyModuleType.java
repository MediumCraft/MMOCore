package net.Indyuce.mmocore.party;

import net.Indyuce.mmocore.party.dungeon.DungeonsPartyModule;
import net.Indyuce.mmocore.party.provided.MMOCorePartyModule;
import org.bukkit.Bukkit;

import javax.inject.Provider;

public enum PartyModuleType {
    DUNGEONSXL("DungeonsXL", () -> new DungeonsPartyModule()),
    MMOCORE("MMOCore", () -> new MMOCorePartyModule()),
    ;

    private final String pluginName;
    private final Provider<PartyModule<?>> provider;

    PartyModuleType(String pluginName, Provider<PartyModule<?>> provider) {
        this.pluginName = pluginName;
        this.provider = provider;
    }

    public boolean isValid() {
        return Bukkit.getPluginManager().getPlugin(pluginName) != null;
    }

    public PartyModule<?> provideModule() {
        return provider.get();
    }
}
