package net.Indyuce.mmocore.party;

import net.Indyuce.mmocore.party.compat.DungeonsPartyModule;
import net.Indyuce.mmocore.party.compat.McMMOPartyModule;
import net.Indyuce.mmocore.party.compat.PAFPartyModule;
import net.Indyuce.mmocore.party.compat.PartiesPartyModule;
import net.Indyuce.mmocore.party.provided.MMOCorePartyModule;
import org.bukkit.Bukkit;

import javax.inject.Provider;

public enum PartyModuleType {
    DUNGEONSXL("DungeonsXL", () -> new DungeonsPartyModule()),
    MMOCORE("MMOCore", () -> new MMOCorePartyModule()),
    PARTY_AND_FRIENDS("PartyAndFriends", () -> new PAFPartyModule()),
    PARTIES("Parties", () -> new PartiesPartyModule()),
    MCMMO("mcMMO", () -> new McMMOPartyModule()),
    // DUNGEONS("Dungeons", null),
    ;

    private final String pluginName;
    private final Provider<PartyModule> provider;

    PartyModuleType(String pluginName, Provider<PartyModule> provider) {
        this.pluginName = pluginName;
        this.provider = provider;
    }

    public boolean isValid() {
        return Bukkit.getPluginManager().getPlugin(pluginName) != null;
    }

    public PartyModule provideModule() {
        return provider.get();
    }
}
