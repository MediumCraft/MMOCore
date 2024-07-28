package net.Indyuce.mmocore.party;

import net.Indyuce.mmocore.party.compat.*;
import net.Indyuce.mmocore.party.provided.MMOCorePartyModule;
import org.bukkit.Bukkit;

import javax.inject.Provider;

public enum PartyModuleType {
    MMOCORE("MMOCore", MMOCorePartyModule::new),
    // DUNGEONS("Dungeons", DungeonsPartyModule::new),
    DUNGEONSXL("DungeonsXL", DungeonsXLPartyModule::new),
    HEROES("Heroes", HeroesPartyModule::new),
    MCMMO("mcMMO", McMMOPartyModule::new),
    MYTHICDUNGEONS_INJECT("MythicDungeons", () -> {
        try {
            return MythicDungeonsPartyInjector.class.getConstructor().newInstance();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }),
    MYTHICDUNGEONS("MythicDungeons", MythicDungeonsPartyModule::new),
    OBTEAM("OBTeam", OBTeamPartyModule::new),
    PARTY_AND_FRIENDS("PartyAndFriends", PAFPartyModule::new),
    PARTY_AND_FRIENDS_BUNGEECORD_VELOCITY("Spigot-Party-API-PAF", PAFProxyPartyModule::new),
    PARTIES("Parties", PartiesPartyModule::new),
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
