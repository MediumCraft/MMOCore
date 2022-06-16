package net.Indyuce.mmocore.guild;

import net.Indyuce.mmocore.guild.compat.GuildsGuildModule;
import net.Indyuce.mmocore.guild.compat.UltimateClansGuildModule;
import net.Indyuce.mmocore.guild.provided.MMOCoreGuildModule;
import org.bukkit.Bukkit;

import javax.inject.Provider;

public enum GuildModuleType {
    // Useless since MythicLib already supports FactionBridge
    // FACTIONS("Factions", FactionsGuildModule::new),
    GUILDS("Guilds", GuildsGuildModule::new),
    KINGDOMSX("Guilds", GuildsGuildModule::new),
    MMOCORE("MMOCore", MMOCoreGuildModule::new),
    ULTIMATE_CLANS("UltimateClans", UltimateClansGuildModule::new),
    ;

    private final String pluginName;
    private final Provider<GuildModule> provider;

    GuildModuleType(String pluginName, Provider<GuildModule> provider) {
        this.pluginName = pluginName;
        this.provider = provider;
    }

    public boolean isValid() {
        return Bukkit.getPluginManager().getPlugin(pluginName) != null;
    }

    public GuildModule provideModule() {
        return provider.get();
    }
}
