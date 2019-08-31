package net.Indyuce.mmocore.command.rpg.waypoint;

import org.bukkit.command.CommandSender;

import net.Indyuce.mmocore.command.api.CommandMap;

public class WaypointsCommandMap extends CommandMap {
	public WaypointsCommandMap(CommandMap parent) {
		super(parent, "waypoints");
		
		addFloor(new UnlockCommandMap(this));
		addFloor(new OpenCommandMap(this));
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		return CommandResult.THROW_USAGE;
	}
}
