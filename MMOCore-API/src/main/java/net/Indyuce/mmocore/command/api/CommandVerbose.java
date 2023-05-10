package net.Indyuce.mmocore.command.api;

import io.lumine.mythic.lib.UtilityMethods;
import net.Indyuce.mmocore.MMOCore;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class CommandVerbose {
    private final Map<CommandType, VerboseValue> values = new HashMap<>();

    public void reload(ConfigurationSection config) {
        values.clear();
        for (CommandType type : CommandType.values())
            try {
                values.put(type, VerboseValue.valueOf(config.getString(UtilityMethods.ymlName(type.name()), "TRUE").toUpperCase()));
            } catch (IllegalArgumentException exception) {
                values.put(type, VerboseValue.TRUE);
                MMOCore.plugin.getLogger().log(Level.WARNING, "Could not load command verbose action for " + type.name());
            }
    }

    public void handle(CommandSender sender, CommandType type, String verbose) {
        switch (values.getOrDefault(type, VerboseValue.FALSE)) {
            case FALSE:
                return;
            case TRUE:
                sender.sendMessage(verbose);
                break;
            case PLAYER:
                if (sender instanceof Player)
                    sender.sendMessage(verbose);
                break;
            case CONSOLE:
                if (sender instanceof ConsoleCommandSender)
                    sender.sendMessage(verbose);
                break;
        }
    }

    public static void verbose(CommandSender sender, CommandType cmd, String verbose) {
        MMOCore.plugin.configManager.commandVerbose.handle(sender, cmd, verbose);
    }

    public enum CommandType {
        ATTRIBUTE,
        SKILL,
        CLASS,
        EXPERIENCE,
        LEVEL,
        NOCD,
        POINTS,
        SKILL_TREE_POINTS,
        RESET,
        RESOURCE,
        WAYPOINT;
    }

    private enum VerboseValue {
        TRUE,
        PLAYER,
        CONSOLE,
        FALSE
    }
}
