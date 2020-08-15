package net.Indyuce.mmocore.command.rpg.waypoint;

import org.bukkit.command.CommandSender;

import net.mmogroup.mmolib.command.api.CommandTreeNode;

public class WaypointsCommandTreeNode extends CommandTreeNode {
	public WaypointsCommandTreeNode(CommandTreeNode parent) {
		super(parent, "waypoints");

		addChild(new UnlockCommandTreeNode(this));
		addChild(new OpenCommandTreeNode(this));
		addChild(new TeleportCommandTreeNode(this));
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		return CommandResult.THROW_USAGE;
	}
}
