package net.Indyuce.mmocore.command.rpg.admin;

import io.lumine.mythic.lib.command.api.CommandTreeNode;
import io.lumine.mythic.lib.command.api.Parameter;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.command.api.CommandVerbose;
import net.Indyuce.mmocore.skill.ClassSkill;
import net.Indyuce.mmocore.skill.binding.BoundSkillInfo;
import net.Indyuce.mmocore.skill.binding.SkillSlot;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class SlotCommandTreeNode extends CommandTreeNode {
    public SlotCommandTreeNode(CommandTreeNode parent) {
        super(parent, "slot");
        addChild(new LockSlotCommandTreeNode(this, "lock", true));
        addChild(new LockSlotCommandTreeNode(this, "unlock", false));
        addChild(new UnbindSlotCommandTreeNode(this, "unbind"));
        addChild(new BindSlotCommandTreeNode(this, "bind"));
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
            SkillSlot skillSlot = playerData.getProfess().getSkillSlot(slot);
            if (skillSlot == null) {
                sender.sendMessage(ChatColor.RED + "Skill slot with index " + slot + " was not found for player " + player.getName() + " with class " + playerData.getProfess().getId());
                return CommandResult.FAILURE;
            }

            if (skillSlot.isUnlockedByDefault()) {
                sender.sendMessage(ChatColor.RED + "You can't lock a skill that is unlocked by default.");
                return CommandResult.FAILURE;
            }
            if (lock) {
                if (!playerData.hasUnlocked(skillSlot)) {
                    CommandVerbose.verbose(sender, CommandVerbose.CommandType.SKILL, ChatColor.RED + "The skill slot " + skillSlot.getName() + " is already locked for " + player.getName());
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
            CommandVerbose.verbose(sender, CommandVerbose.CommandType.SKILL, ChatColor.YELLOW + "The skill slot " + skillSlot.getName() + " is now " + (lock ? "locked" : "unlocked" + " for " + player.getName()));
            return CommandResult.SUCCESS;
        }
    }


    public class BindSlotCommandTreeNode extends CommandTreeNode {

        public BindSlotCommandTreeNode(CommandTreeNode parent, String id) {
            super(parent, id);
            addParameter(Parameter.PLAYER);
            addParameter(Parameter.AMOUNT);
            addParameter(new Parameter("<skill>",
                    (explorer, list) -> MMOCore.plugin.skillManager.getAll().forEach(skill -> list.add(skill.getHandler().getId().toUpperCase()))));

        }

        @Override
        public CommandResult execute(CommandSender sender, String[] args) {
            if (args.length < 6)
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
            ClassSkill skill = playerData.getProfess().getSkill(args[5]);
            if (skill == null) {
                sender.sendMessage(ChatColor.RED + "The player's class doesn't have a skill called  " + args[5] + ".");
                return CommandResult.FAILURE;
            }
            playerData.bindSkill(slot, skill);

            CommandVerbose.verbose(sender, CommandVerbose.CommandType.SKILL, ChatColor.YELLOW + "Skill " + ChatColor.GOLD + skill.getSkill().getHandler().getId() + ChatColor.YELLOW + " now bound to slot " + ChatColor.GOLD + slot);
            return CommandResult.SUCCESS;
        }
    }

    public class UnbindSlotCommandTreeNode extends CommandTreeNode {

        public UnbindSlotCommandTreeNode(CommandTreeNode parent, String id) {
            super(parent, id);
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
            final BoundSkillInfo found = playerData.unbindSkill(slot);
            CommandVerbose.verbose(sender, CommandVerbose.CommandType.SKILL, ChatColor.YELLOW + (found != null ?
                    "Skill " + ChatColor.GOLD + found.getClassSkill().getSkill().getName() + ChatColor.YELLOW + " was taken off the slot " + ChatColor.GOLD + slot :
                    "Could not find skill at slot " + ChatColor.GOLD + slot));
            return CommandResult.SUCCESS;
        }
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        return CommandResult.THROW_USAGE;
    }
}
