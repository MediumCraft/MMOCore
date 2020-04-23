package net.Indyuce.mmocore.command.rpg.debug;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import net.Indyuce.mmocore.command.api.CommandMap;

public class DebugCommandMap extends CommandMap {
	public static final String commandPrefix = ChatColor.YELLOW + "[" + ChatColor.RED + "DEBUG" + ChatColor.GOLD + "] " + ChatColor.RESET;

	public DebugCommandMap(CommandMap parent) {
		super(parent, "debug");

		addFloor(new StatValueCommandMap(this));
		addFloor(new StatModifiersCommandMap(this));
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		return CommandResult.THROW_USAGE;
	}
}
