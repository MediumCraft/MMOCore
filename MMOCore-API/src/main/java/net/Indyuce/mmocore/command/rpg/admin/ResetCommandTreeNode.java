package net.Indyuce.mmocore.command.rpg.admin;

import io.lumine.mythic.lib.command.api.CommandTreeNode;
import io.lumine.mythic.lib.command.api.Parameter;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.attribute.PlayerAttribute;
import net.Indyuce.mmocore.api.player.attribute.PlayerAttributes;
import net.Indyuce.mmocore.command.api.CommandVerbose;
import net.Indyuce.mmocore.experience.Profession;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public class ResetCommandTreeNode extends CommandTreeNode {
    public ResetCommandTreeNode(CommandTreeNode parent) {
        super(parent, "reset");

        addChild(new ResetClassesCommandTreeNode(this));
        addChild(new ResetLevelsCommandTreeNode(this));
        addChild(new ResetSkillsCommandTreeNode(this));
        addChild(new ResetQuestsCommandTreeNode(this));
        addChild(new ResetAttributesCommandTreeNode(this));
        addChild(new ResetWaypointsCommandTreeNode(this));
        addChild(new ResetSkillTreesCommandTreeNode(this));
        addChild(new ResetAllCommandTreeNode(this));
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        return CommandResult.THROW_USAGE;
    }

    public static class ResetAllCommandTreeNode extends CommandTreeNode {
        public ResetAllCommandTreeNode(CommandTreeNode parent) {
            super(parent, "all");

            addParameter(Parameter.PLAYER);
        }

        @Override
        public CommandResult execute(CommandSender sender, String[] args) {
            if (args.length < 4) return CommandResult.THROW_USAGE;

            Player player = Bukkit.getPlayer(args[3]);
            if (player == null) {
                sender.sendMessage(ChatColor.RED + "Could not find the player called " + args[3] + ".");
                return CommandResult.FAILURE;
            }

            final boolean givePoints = args.length > 4 && args[4].equalsIgnoreCase("-reallocate");

            PlayerData data = PlayerData.get(player);
            ResetClassesCommandTreeNode.resetClasses(data);
            ResetLevelsCommandTreeNode.resetLevels(data);
            ResetSkillsCommandTreeNode.resetSkills(data);
            ResetQuestsCommandTreeNode.resetQuests(data);
            ResetAttributesCommandTreeNode.resetAttributes(data, givePoints);
            ResetWaypointsCommandTreeNode.resetWaypoints(data);
            ResetSkillTreesCommandTreeNode.resetSkillTrees(data);
            CommandVerbose.verbose(sender, CommandVerbose.CommandType.RESET, ChatColor.GOLD + player.getName() + ChatColor.YELLOW + "'s data was successfully reset.");
            return CommandResult.SUCCESS;
        }
    }
}

class ResetWaypointsCommandTreeNode extends CommandTreeNode {
    public ResetWaypointsCommandTreeNode(CommandTreeNode parent) {
        super(parent, "waypoints");

        addParameter(Parameter.PLAYER);
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        if (args.length < 4) return CommandResult.THROW_USAGE;

        Player player = Bukkit.getPlayer(args[3]);
        if (player == null) {
            sender.sendMessage(ChatColor.RED + "Could not find the player called " + args[3] + ".");
            return CommandResult.FAILURE;
        }

        resetWaypoints(PlayerData.get(player));
        CommandVerbose.verbose(sender, CommandVerbose.CommandType.RESET, ChatColor.GOLD + player.getName() + ChatColor.YELLOW + "'s waypoints were successfully reset.");
        return CommandResult.SUCCESS;
    }

    static void resetWaypoints(@NotNull PlayerData playerData) {
        playerData.getWaypoints().clear();
    }
}

class ResetQuestsCommandTreeNode extends CommandTreeNode {
    public ResetQuestsCommandTreeNode(CommandTreeNode parent) {
        super(parent, "quests");

        addParameter(Parameter.PLAYER);
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        if (args.length < 4) return CommandResult.THROW_USAGE;

        Player player = Bukkit.getPlayer(args[3]);
        if (player == null) {
            sender.sendMessage(ChatColor.RED + "Could not find the player called " + args[3] + ".");
            return CommandResult.FAILURE;
        }

        resetQuests(PlayerData.get(player));
        CommandVerbose.verbose(sender, CommandVerbose.CommandType.RESET, ChatColor.GOLD + player.getName() + ChatColor.YELLOW + "'s quests were successfully reset.");
        return CommandResult.SUCCESS;
    }

    static void resetQuests(@NotNull PlayerData data) {
        data.getQuestData().resetFinishedQuests();
        data.getQuestData().start(null);
    }
}

class ResetSkillsCommandTreeNode extends CommandTreeNode {
    public ResetSkillsCommandTreeNode(CommandTreeNode parent) {
        super(parent, "skills");

        addParameter(Parameter.PLAYER);
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        if (args.length < 4) return CommandResult.THROW_USAGE;

        Player player = Bukkit.getPlayer(args[3]);
        if (player == null) {
            sender.sendMessage(ChatColor.RED + "Could not find the player called " + args[3] + ".");
            return CommandResult.FAILURE;
        }

        resetSkills(PlayerData.get(player));
        CommandVerbose.verbose(sender, CommandVerbose.CommandType.RESET, ChatColor.GOLD + player.getName() + ChatColor.YELLOW + "'s skill data was successfully reset.");
        return CommandResult.SUCCESS;
    }

    static void resetSkills(@NotNull PlayerData data) {
        data.mapSkillLevels().forEach((skill, ignored) -> data.resetSkillLevel(skill));
        while (data.hasSkillBound(0)) data.unbindSkill(0);
        data.setUnlockedItems(new HashSet<>()); // TODO class-specific unlockables etc.
    }
}

class ResetSkillTreesCommandTreeNode extends CommandTreeNode {
    public ResetSkillTreesCommandTreeNode(CommandTreeNode parent) {
        super(parent, "skill-trees");

        addParameter(Parameter.PLAYER);
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        if (args.length < 4) return CommandResult.THROW_USAGE;

        Player player = Bukkit.getPlayer(args[3]);
        if (player == null) {
            sender.sendMessage(ChatColor.RED + "Could not find the player called " + args[3] + ".");
            return CommandResult.FAILURE;
        }

        resetSkillTrees(PlayerData.get(player));
        CommandVerbose.verbose(sender, CommandVerbose.CommandType.RESET, ChatColor.GOLD + player.getName() + ChatColor.YELLOW + "'s skill-tree data was successfully reset.");
        return CommandResult.SUCCESS;
    }

    // TODO option to reallocate skill tree points instead of not giving any back
    static void resetSkillTrees(@NotNull PlayerData data) {
        data.resetSkillTrees();
    }
}

class ResetAttributesCommandTreeNode extends CommandTreeNode {
    public ResetAttributesCommandTreeNode(CommandTreeNode parent) {
        super(parent, "attributes");

        addParameter(Parameter.PLAYER);
        addParameter(new Parameter("(-reallocate)", (explore, list) -> list.add("-reallocate")));
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        if (args.length < 4) return CommandResult.THROW_USAGE;

        Player player = Bukkit.getPlayer(args[3]);
        if (player == null) {
            sender.sendMessage(ChatColor.RED + "Could not find the player called " + args[3] + ".");
            return CommandResult.FAILURE;
        }

        final boolean givePoints = args.length > 4 && args[4].equalsIgnoreCase("-reallocate");
        resetAttributes(PlayerData.get(player), givePoints);
        CommandVerbose.verbose(sender, CommandVerbose.CommandType.RESET, ChatColor.GOLD + player.getName() + ChatColor.YELLOW + "'s attributes were successfully reset.");
        return CommandResult.SUCCESS;
    }

    static void resetAttributes(@NotNull PlayerData data, boolean givePoints) {

        // Give back attribute points
        if (givePoints) {

            int points = 0;
            for (PlayerAttributes.AttributeInstance ins : data.getAttributes().getInstances()) {
                points += ins.getBase();
                ins.setBase(0);
            }

            data.giveAttributePoints(points);
            return;
        }

        for (PlayerAttribute attribute : MMOCore.plugin.attributeManager.getAll()) {
            attribute.resetAdvancement(data, true);
            data.getAttributes().getInstance(attribute).setBase(0);
        }
    }
}

class ResetLevelsCommandTreeNode extends CommandTreeNode {
    public ResetLevelsCommandTreeNode(CommandTreeNode parent) {
        super(parent, "levels");

        addParameter(Parameter.PLAYER);
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        if (args.length < 4) return CommandResult.THROW_USAGE;

        Player player = Bukkit.getPlayer(args[3]);
        if (player == null) {
            sender.sendMessage(ChatColor.RED + "Could not find the player called " + args[3] + ".");
            return CommandResult.FAILURE;
        }

        resetLevels(PlayerData.get(player));
        CommandVerbose.verbose(sender, CommandVerbose.CommandType.RESET, ChatColor.GOLD + player.getName() + ChatColor.YELLOW + "'s levels were successfully reset.");
        return CommandResult.SUCCESS;
    }

    static void resetLevels(@NotNull PlayerData data) {

        // Class
        data.setLevel(MMOCore.plugin.playerDataManager.getDefaultData().getLevel());
        data.setExperience(0);
        data.getProfess().resetAdvancement(data, true);

        // Professions
        for (Profession profession : MMOCore.plugin.professionManager.getAll()) {
            data.getCollectionSkills().setExperience(profession, 0);
            data.getCollectionSkills().setLevel(profession, 0);
            profession.resetAdvancement(data, true);
        }
    }
}

class ResetClassesCommandTreeNode extends CommandTreeNode {
    public ResetClassesCommandTreeNode(CommandTreeNode parent) {
        super(parent, "classes");

        addParameter(Parameter.PLAYER);
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        if (args.length < 4) return CommandResult.THROW_USAGE;

        Player player = Bukkit.getPlayer(args[3]);
        if (player == null) {
            sender.sendMessage(ChatColor.RED + "Could not find the player called " + args[3] + ".");
            return CommandResult.FAILURE;
        }

        resetClasses(PlayerData.get(player));
        CommandVerbose.verbose(sender, CommandVerbose.CommandType.RESET, ChatColor.GOLD + player.getName() + ChatColor.YELLOW + "'s classes were successfully reset.");
        return CommandResult.SUCCESS;
    }

    static void resetClasses(@NotNull PlayerData data) {
        MMOCore.plugin.classManager.getAll().forEach(data::unloadClassInfo);
        MMOCore.plugin.playerDataManager.getDefaultData().apply(data);
        data.setClass(MMOCore.plugin.classManager.getDefaultClass());
    }
}