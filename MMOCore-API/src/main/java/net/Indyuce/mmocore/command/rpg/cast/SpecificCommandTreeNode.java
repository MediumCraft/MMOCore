package net.Indyuce.mmocore.command.rpg.cast;

import io.lumine.mythic.lib.command.api.CommandTreeNode;
import io.lumine.mythic.lib.command.api.Parameter;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.ClassSkill;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class SpecificCommandTreeNode extends CommandTreeNode {
    public static final Parameter INTEGER = new Parameter("slot", false, (explorer, list) -> list.addAll(Arrays.asList("1", "2", "3", "4", "5", "6")));

    public SpecificCommandTreeNode(CommandTreeNode parent) {
        super(parent, "specific");

        addParameter(Parameter.PLAYER);
        addParameter(INTEGER);
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        if (args.length < 3) return CommandResult.THROW_USAGE;

        Player player = Bukkit.getPlayer(args[2]);
        if (player == null) {
            sender.sendMessage(ChatColor.RED + "Could not find the player called " + args[2] + ".");
            return CommandResult.FAILURE;
        }
        PlayerData data = PlayerData.get(player);

        int slot;
        try {
            slot = Integer.parseInt(args[3]);
            Validate.isTrue(slot > 0);
        } catch (Exception exception) {
            sender.sendMessage(ChatColor.RED + args[3] + " is not a valid integer.");
            return CommandResult.FAILURE;
        }

        ClassSkill skill = data.getBoundSkill(slot);
        if (skill == null) {
            sender.sendMessage(ChatColor.RED + "Found no skill bound to slot " + slot + " of player " + player.getName() + ".");
            return CommandResult.FAILURE;
        }

        if (skill.getSkill().getTrigger().isPassive()) {
            sender.sendMessage(ChatColor.RED + "Skill '" + skill.getSkill().getName() + "' bound to slot " + slot + " is passive.");
            return CommandResult.FAILURE;
        }

        boolean success = skill.toCastable(data).cast(data.getMMOPlayerData()).isSuccessful();
        return success ? CommandResult.SUCCESS : CommandResult.FAILURE;
    }
}
