package net.Indyuce.mmocore.command.rpg.waypoint;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.Waypoint;
import net.mmogroup.mmolib.command.api.CommandTreeNode;
import net.mmogroup.mmolib.command.api.Parameter;

public class TeleportCommandTreeNode extends CommandTreeNode {
	public TeleportCommandTreeNode(CommandTreeNode parent) {
		super(parent, "teleport");

		addParameter(new Parameter("<waypoint>", (explorer, list) -> MMOCore.plugin.waypointManager.getAll().forEach(way -> list.add(way.getId()))));
		addParameter(Parameter.PLAYER);
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		if (args.length < 4)
			return CommandResult.THROW_USAGE;

		if (!MMOCore.plugin.waypointManager.has(args[2])) {
			sender.sendMessage(ChatColor.RED + "Could not find waypoint " + args[2]);
			return CommandResult.FAILURE;
		}

		Player player = Bukkit.getPlayer(args[3]);
		if (player == null) {
			sender.sendMessage(ChatColor.RED + "Could not find player " + args[3]);
			return CommandResult.FAILURE;
		}

		Waypoint waypoint = MMOCore.plugin.waypointManager.get(args[2]);
		player.teleport(waypoint.getLocation());
		sender.sendMessage(ChatColor.YELLOW + "Successfully teleported " + ChatColor.GOLD + player.getName() + ChatColor.YELLOW + " to "
				+ ChatColor.GOLD + waypoint.getId() + ChatColor.YELLOW + ".");
		return CommandResult.SUCCESS;
	}
}
