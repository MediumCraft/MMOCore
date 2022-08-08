package net.Indyuce.mmocore.command.rpg.admin;

import io.lumine.mythic.lib.command.api.CommandTreeNode;
import io.lumine.mythic.lib.command.api.Parameter;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.command.CommandVerbose;
import net.Indyuce.mmocore.waypoint.Waypoint;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class WaypointCommandTreeNode extends CommandTreeNode {
    public WaypointCommandTreeNode(CommandTreeNode parent) {
        super(parent, "waypoint");
        addChild(new ActionCommandTreeNode(this, "unlock",
                (playerData, waypoint) -> !playerData.hasWaypoint(waypoint)
                , (playerData, waypoint) -> playerData.unlockWaypoint(waypoint)));
        addChild(new ActionCommandTreeNode(this, "lock",
                (playerData, waypoint) -> playerData.hasWaypoint(waypoint)
                , (playerData, waypoint) -> playerData.lockWaypoint(waypoint)));
    }

    @Override
    public CommandResult execute(CommandSender commandSender, String[] strings) {
        return null;
    }

    private class ActionCommandTreeNode extends CommandTreeNode {
        private final BiFunction<PlayerData, Waypoint, Boolean> check;
        private final BiConsumer<PlayerData, Waypoint> change;


        public ActionCommandTreeNode(CommandTreeNode parent, String id,
                                     BiFunction<PlayerData, Waypoint, Boolean> check,
                                     BiConsumer<PlayerData, Waypoint> change) {
            super(parent, id);
            this.change = change;
            this.check = check;
            addParameter(Parameter.PLAYER);
            addParameter(new Parameter("waypoint", ((commandTreeExplorer, list) ->
                    MMOCore.plugin.waypointManager.getAll().forEach(waypoint -> list.add(waypoint.getId()))
            )));
        }

        @Override
        public CommandResult execute(CommandSender sender, String[] args) {
            Player player = Bukkit.getPlayer(args[3]);
            if (player == null) {
                sender.sendMessage(ChatColor.RED + "Could not find the player called " + args[3] + ".");
                return CommandResult.FAILURE;
            }

            Waypoint waypoint = MMOCore.plugin.waypointManager.get(args[4]);
            if (waypoint == null) {
                sender.sendMessage(ChatColor.RED + "Could not find the waypoint called " + args[4] + ".");
                return CommandResult.FAILURE;
            }

            PlayerData playerData = PlayerData.get(player);
            if (!check.apply(playerData, waypoint)) {
                sender.sendMessage(ChatColor.RED + "The waypoint " + args[4] + " is already in this state.");
                return CommandResult.FAILURE;
            }
            change.accept(playerData, waypoint);
            return CommandResult.SUCCESS;

        }
    }

}
