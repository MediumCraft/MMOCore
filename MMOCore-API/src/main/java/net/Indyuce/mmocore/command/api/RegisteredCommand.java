package net.Indyuce.mmocore.command.api;

import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public abstract class RegisteredCommand extends BukkitCommand {
    public RegisteredCommand(@NotNull ConfigurationSection config, ToggleableCommand command) {
        super(config.getString("main"));

        setAliases(config.getStringList("aliases"));
        setDescription(config.getString("description", command.getDescription()));
    }
}
