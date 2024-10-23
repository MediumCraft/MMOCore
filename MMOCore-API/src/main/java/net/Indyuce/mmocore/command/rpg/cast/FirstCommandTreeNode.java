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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FirstCommandTreeNode extends CommandTreeNode {
    public FirstCommandTreeNode(CommandTreeNode parent) {
        super(parent, "first");

        addParameter(Parameter.PLAYER);
        addParameter(SpecificCommandTreeNode.INTEGER);
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

        List<Integer> slots = data.getBoundSkills().entrySet().stream()
                .filter(e -> !e.getValue().isPassive())
                .map(Map.Entry::getKey)
                .sorted(Integer::compare).collect(Collectors.toList());
        if (slot > slots.size()) {
            sender.sendMessage(ChatColor.RED + "Player " + player.getName() + " only has active skills on slots " + slots + ".");
            return CommandResult.FAILURE;
        }

        ClassSkill skill = data.getBoundSkill(slots.get(slot - 1));
        Validate.notNull(skill, "Internal error: skill is null");
        Validate.isTrue(!skill.getSkill().getTrigger().isPassive(), "Internal error: skill is passive");

        boolean success = skill.toCastable(data).cast(data.getMMOPlayerData()).isSuccessful();
        return success ? CommandResult.SUCCESS : CommandResult.FAILURE;
    }
}
