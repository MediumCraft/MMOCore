package net.Indyuce.mmocore.command.rpg.waypoint;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.Indyuce.mmocore.manager.InventoryManager;
import io.lumine.mythic.lib.commands.mmolib.api.CommandTreeNode;
import io.lumine.mythic.lib.commands.mmolib.api.Parameter;

public class OpenCommandTreeNode extends CommandTreeNode {
	public OpenCommandTreeNode(CommandTreeNode parent) {
		super(parent, "open");

		addParameter(Parameter.PLAYER);
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		if (args.length < 3)
			return CommandResult.THROW_USAGE;

		Player player = Bukkit.getPlayer(args[2]);
		if (player == null) {
			sender.sendMessage(ChatColor.RED + "Could not find player " + args[2]);
			return CommandResult.FAILURE;
		}

		InventoryManager.WAYPOINTS.newInventory(net.Indyuce.mmocore.api.player.PlayerData.get(player)).open();
		return CommandResult.SUCCESS;
	}
}
