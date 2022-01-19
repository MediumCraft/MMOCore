package net.Indyuce.mmocore.guild;

import org.bukkit.Bukkit;

import javax.inject.Provider;

public enum GuildModuleType {
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
