package net.Indyuce.mmocore.command.rpg.waypoint;

import io.lumine.mythic.lib.api.util.SmartGive;
import io.lumine.mythic.lib.command.api.CommandTreeNode;
import io.lumine.mythic.lib.command.api.Parameter;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.waypoint.Waypoint;
import net.Indyuce.mmocore.util.item.WaypointBookBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ItemCommandTreeNode extends CommandTreeNode {
    public ItemCommandTreeNode(CommandTreeNode parent) {
        super(parent, "item");

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
        new SmartGive(player).give(new WaypointBookBuilder(waypoint).build());
        sender.sendMessage(ChatColor.GOLD + "Gave " + player.getName() + ChatColor.YELLOW + " a waypoint book of " + ChatColor.GOLD + waypoint.getId()
                + ChatColor.YELLOW + ".");
        return CommandResult.SUCCESS;
    }
}
