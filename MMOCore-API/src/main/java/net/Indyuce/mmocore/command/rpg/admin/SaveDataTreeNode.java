package net.Indyuce.mmocore.command.rpg.admin;

import io.lumine.mythic.lib.command.api.CommandTreeNode;
import io.lumine.mythic.lib.command.api.Parameter;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Saves player data
 */
public class SaveDataTreeNode extends CommandTreeNode {
    public SaveDataTreeNode(CommandTreeNode parent) {
        super(parent, "savedata");

        addParameter(Parameter.PLAYER);
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        if (args.length < 3)
            return CommandResult.THROW_USAGE;

        Player player = Bukkit.getPlayer(args[2]);
        if (player == null) {
            sender.sendMessage(ChatColor.RED + "Could not find the player called " + args[2] + ".");
            return CommandResult.FAILURE;
        }

        MMOCore.plugin.dataProvider.getDataManager().getDataHandler().saveData(PlayerData.get(player), false);

        return CommandResult.SUCCESS;
    }
}
