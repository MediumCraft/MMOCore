package net.Indyuce.mmocore.guild;

import net.Indyuce.mmocore.guild.compat.FactionsGuildModule;
import net.Indyuce.mmocore.guild.compat.UltimateClansGuildModule;
import org.bukkit.Bukkit;

import javax.inject.Provider;

public enum GuildModuleType {
    FACTIONS("Factions", FactionsGuildModule::new),
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
