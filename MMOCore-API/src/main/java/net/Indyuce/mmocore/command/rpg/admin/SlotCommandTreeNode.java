package net.Indyuce.mmocore.command.rpg.admin;

import io.lumine.mythic.lib.command.api.CommandTreeNode;
import io.lumine.mythic.lib.command.api.Parameter;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.skillbinding.SkillSlot;
import net.Indyuce.mmocore.command.api.CommandVerbose;
import net.Indyuce.mmocore.skill.ClassSkill;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class SlotCommandTreeNode extends CommandTreeNode {
    public SlotCommandTreeNode(CommandTreeNode parent) {
        super(parent, "skill");
        addChild(new LockSlotCommandTreeNode(this, "lock", true));
        addChild(new LockSlotCommandTreeNode(this, "unlock", false));
    }

    public class LockSlotCommandTreeNode extends CommandTreeNode {
        private final boolean lock;

        public LockSlotCommandTreeNode(CommandTreeNode parent, String id, boolean lock) {
            super(parent, id);
            this.lock = lock;
            addParameter(Parameter.PLAYER);
            addParameter(Parameter.AMOUNT);
        }

        @Override
        public CommandResult execute(CommandSender sender, String[] args) {
            if (args.length < 5)
                return CommandResult.THROW_USAGE;
            Player player = Bukkit.getPlayer(args[3]);
            if (player == null) {
                sender.sendMessage(ChatColor.RED + "Could not find the player called " + args[3] + ".");
                return CommandResult.FAILURE;
            }
            PlayerData playerData = PlayerData.get(player);
            int slot;
            try {
                slot = Integer.parseInt(args[4]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + args[4] + " is not a valid number.");
                return CommandResult.FAILURE;
            }
            if (slot <= 0) {
                sender.sendMessage(ChatColor.RED + "The slot can't be negative.");
                return CommandResult.FAILURE;
            }
            SkillSlot skillSlot=playerData.getProfess().getSkillSlot(slot);
            if (lock) {
                if (!playerData.hasUnlocked(skillSlot)) {
                    CommandVerbose.verbose(sender, CommandVerbose.CommandType.SKILL, ChatColor.RED + "The skill slot " + skillSlot.getName() + " is already locked" + " for " + player.getName());
                    return CommandResult.SUCCESS;
                }
                playerData.lock(skillSlot);

            } else {
                if (playerData.hasUnlocked(skillSlot)) {
                    CommandVerbose.verbose(sender, CommandVerbose.CommandType.SKILL, ChatColor.RED + "The skill slot " + skillSlot.getName() + " is already unlocked" + " for " + player.getName());
                    return CommandResult.SUCCESS;
                }
                playerData.unlock(skillSlot);
            }
            CommandVerbose.verbose(sender, CommandVerbose.CommandType.SKILL, ChatColor.GOLD + "The skill slot " + skillSlot.getName() + " is now " + (lock ? "locked" : "unlocked" + " for " + player.getName()));
            return CommandResult.SUCCESS;
        }
    }


    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        return CommandResult.THROW_USAGE;
    }
}
