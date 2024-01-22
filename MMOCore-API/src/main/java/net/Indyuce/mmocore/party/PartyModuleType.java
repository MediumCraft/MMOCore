package net.Indyuce.mmocore.party;

import net.Indyuce.mmocore.party.compat.*;
import net.Indyuce.mmocore.party.provided.MMOCorePartyModule;
import org.bukkit.Bukkit;

import javax.inject.Provider;

public enum PartyModuleType {
    // DUNGEONS("Dungeons", DungeonsPartyModule::new),
    DUNGEONSXL("DungeonsXL", DungeonsXLPartyModule::new),
    MCMMO("mcMMO", McMMOPartyModule::new),
    MMOCORE("MMOCore", MMOCorePartyModule::new),
    PARTIES("Parties", PartiesPartyModule::new),
    MYTHICDUNGEONS("MythicDungeons", DungeonPartiesPartyModule::new),
    OBTEAM("OBTeam", OBTeamPartyModule::new),
    PARTY_AND_FRIENDS("PartyAndFriends", PAFPartyModule::new),
    PARTY_AND_FRIENDS_BUNGEECORD_VELOCITY("Spigot-Party-API-PAF", PAFProxyPartyModule::new),
    ;

    private final String pluginName;
    private final Provider<PartyModule> provider;

    PartyModuleType(String pluginName, Provider<PartyModule> provider) {
        this.pluginName = pluginName;
        this.provider = provider;
    }

    public String getPluginName() {
        return pluginName;
    }

    public boolean isValid() {
        return Bukkit.getPluginManager().getPlugin(pluginName) != null;
    }

    public PartyModule provideModule() {
        return provider.get();
    }
}
