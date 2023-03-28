package net.Indyuce.mmocore.command.rpg.admin;

import io.lumine.mythic.lib.command.api.CommandTreeNode;
import io.lumine.mythic.lib.command.api.Parameter;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.command.api.CommandVerbose;
import net.Indyuce.mmocore.skill.ClassSkill;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.function.BiFunction;


public class SkillCommandTreeNode extends CommandTreeNode {
    public SkillCommandTreeNode(CommandTreeNode parent) {
        super(parent, "skill");
        addChild(new LockSkillCommandTreeNode(this, "lock", true));
        addChild(new LockSkillCommandTreeNode(this, "unlock", false));
        addChild(new ActionCommandTreeNode(this, "give", (old, amount) -> old + amount));
        addChild(new ActionCommandTreeNode(this, "set", (old, amount) -> amount));
    }


    public class ActionCommandTreeNode extends CommandTreeNode {
        private final BiFunction<Integer, Integer, Integer> change;

        public ActionCommandTreeNode(CommandTreeNode parent, String type, BiFunction<Integer, Integer, Integer> change) {
            super(parent, type);
            this.change = change;
            addParameter(Parameter.PLAYER);
            addParameter(new Parameter("<attribute>",
                    (explorer, list) -> MMOCore.plugin.skillManager.getAll().forEach(skill -> list.add(skill.getHandler().getId().toUpperCase()))));
            addParameter(Parameter.AMOUNT);
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

            RegisteredSkill skill = MMOCore.plugin.skillManager.getSkill(args[4]);
            if (skill == null) {
                sender.sendMessage(ChatColor.RED + "Could not find the skill called " + args[4] + ".");
                return CommandResult.FAILURE;
            }


            ClassSkill classSkill = null;
            for (ClassSkill var : playerData.getProfess().getSkills()) {
                if (var.getSkill().equals(skill))
                    classSkill = var;
            }

            if (classSkill == null || classSkill.getUnlockLevel() > playerData.getLevel()) {
                sender.sendMessage(ChatColor.RED + skill.getName() + " is not unlockable for " + player.getName() + ".");
                return CommandResult.FAILURE;
            }


            int amount;
            try {
                amount = Integer.parseInt(args[5]);
            } catch (Exception e) {
                sender.sendMessage(ChatColor.RED + args[5] + " is not a valid number.");
                return CommandResult.FAILURE;
            }
            int value = change.apply(playerData.getSkillLevel(skill), amount);
            playerData.setSkillLevel(skill, value);
            CommandVerbose.verbose(sender, CommandVerbose.CommandType.SKILL, ChatColor.GOLD + player.getName() + ChatColor.YELLOW
                    + " is now level  " + ChatColor.GOLD + value + ChatColor.YELLOW + " for " + skill.getName() + ".");
            return CommandResult.SUCCESS;
        }
    }

    public class LockSkillCommandTreeNode extends CommandTreeNode {
        private final boolean lock;

        public LockSkillCommandTreeNode(CommandTreeNode parent, String id, boolean lock) {
            super(parent, id);
            this.lock = lock;
            addParameter(Parameter.PLAYER);
        }

        @Override
        public CommandResult execute(CommandSender sender, String[] args) {
            if (args.length < 4)
                return CommandResult.THROW_USAGE;
            Player player = Bukkit.getPlayer(args[3]);
            if (player == null) {
                sender.sendMessage(ChatColor.RED + "Could not find the player called " + args[3] + ".");
                return CommandResult.FAILURE;
            }
            PlayerData playerData = PlayerData.get(player);

            ClassSkill skill = playerData.getProfess().getSkill(args[4]);
            if (skill == null) {
                sender.sendMessage(ChatColor.RED + "The player's class doesn't have a skill called  " + args[4] + ".");
                return CommandResult.FAILURE;
            }
            if (lock)
                playerData.getMMOPlayerData().lock(skill.getSkill());
            else
                playerData.getMMOPlayerData().unlock(skill.getSkill());
            CommandVerbose.verbose(sender, CommandVerbose.CommandType.SKILL, ChatColor.GOLD+"The skill " + skill.getSkill().getName() + " is now " + (lock ? "locked" : "unlocked" + " for " + player.getName()));
            return CommandResult.SUCCESS;
        }
    }


    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        return CommandResult.THROW_USAGE;
    }
}
