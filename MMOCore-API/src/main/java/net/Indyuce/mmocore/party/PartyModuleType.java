package net.Indyuce.mmocore.party;

import net.Indyuce.mmocore.party.compat.DungeonsXLPartyModule;
import net.Indyuce.mmocore.party.compat.McMMOPartyModule;
import net.Indyuce.mmocore.party.compat.PAFPartyModule;
import net.Indyuce.mmocore.party.provided.MMOCorePartyModule;
import net.Indyuce.mmocore.party.compat.PartiesPartyModule;
import org.bukkit.Bukkit;

import javax.inject.Provider;

public enum PartyModuleType {
    // DUNGEONS("Dungeons", DungeonsPartyModule::new),
    DUNGEONSXL("DungeonsXL", DungeonsXLPartyModule::new),
    MCMMO("mcMMO", McMMOPartyModule::new),
    MMOCORE("MMOCore", MMOCorePartyModule::new),
    PARTIES("Parties", PartiesPartyModule::new),
    PARTY_AND_FRIENDS("PartyAndFriends", PAFPartyModule::new),
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
