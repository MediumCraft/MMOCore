package net.Indyuce.mmocore.command;

import io.lumine.mythic.lib.api.util.EnumUtils;
import net.Indyuce.mmocore.MMOCore;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class CommandVerbose {
	private final Map<CommandType, VerboseValue> values = new HashMap<>();

	public void reload(ConfigurationSection config) {
		values.clear();

		for(CommandType type : CommandType.values())
			values.put(type, EnumUtils.getIfPresent(VerboseValue.class, config.getString(type.name().toLowerCase(), "true")).orElse(VerboseValue.TRUE));
	}

	public enum CommandType {
		ATTRIBUTE, CLASS, EXPERIENCE, LEVEL,
		NOCD, POINTS, RESET, RESOURCE
	}

	enum VerboseValue {
		TRUE, PLAYER,
		CONSOLE, FALSE
	}

	public void handle(CommandSender sender, CommandType cmd, String verbose) {
		switch(values.getOrDefault(cmd, VerboseValue.FALSE)) {
			case FALSE:
				return;
			case TRUE:
				//sender.sendMessage(verbose);
				break;
			case PLAYER:
				if(sender instanceof Player)
					//sender.sendMessage(verbose);
				break;
			case CONSOLE:
				if(!(sender instanceof Player))
					//sender.sendMessage(verbose);
				break;
		}
	}

	public static void verbose(CommandSender sender, CommandType cmd, String verbose) {
		MMOCore.plugin.configManager.commandVerbose.handle(sender, cmd, verbose);
	}
}
