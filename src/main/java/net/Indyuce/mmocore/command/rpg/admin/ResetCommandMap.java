package net.Indyuce.mmocore.command.rpg.admin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.experience.Profession;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.command.api.CommandEnd;
import net.Indyuce.mmocore.command.api.CommandMap;
import net.Indyuce.mmocore.command.api.Parameter;

public class ResetCommandMap extends CommandEnd {
	public ResetCommandMap(CommandMap parent) {
		super(parent, "reset");

		addFloor(new ResetLevelsCommandMap(this));
		addFloor(new ResetSkillsCommandMap(this));
		addFloor(new ResetAllCommandMap(this));
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		return CommandResult.THROW_USAGE;
	}

	public class ResetAllCommandMap extends CommandEnd {
		public ResetAllCommandMap(CommandMap parent) {
			super(parent, "all");

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

			PlayerData data = PlayerData.get(player);
			data.setLevel(1);
			data.setExperience(0);
			for (Profession profession : MMOCore.plugin.professionManager.getAll()) {
				data.getCollectionSkills().setExperience(profession, 0);
				data.getCollectionSkills().setLevel(profession, 0);
			}
			MMOCore.plugin.classManager.getAll().forEach(profess -> data.unloadClassInfo(profess));
			data.setClassPoints(0);
			data.setSkillPoints(0);
			MMOCore.plugin.skillManager.getAll().forEach(skill -> data.lockSkill(skill));
			while (data.hasSkillBound(0))
				data.unbindSkill(0);
			data.getQuestData().resetFinishedQuests();
			data.getQuestData().start(null);
			sender.sendMessage(ChatColor.GOLD + player.getName() + ChatColor.YELLOW + "'s data was succesfully reset.");
			return CommandResult.SUCCESS;
		}
	}

	public class ResetQuestsCommandMap extends CommandEnd {
		public ResetQuestsCommandMap(CommandMap parent) {
			super(parent, "quests");

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

			PlayerData data = PlayerData.get(player);
			data.getQuestData().resetFinishedQuests();
			data.getQuestData().start(null);
			return CommandResult.SUCCESS;
		}
	}

	public class ResetSkillsCommandMap extends CommandEnd {
		public ResetSkillsCommandMap(CommandMap parent) {
			super(parent, "skills");

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

			PlayerData data = PlayerData.get(player);
			data.setSkillPoints(0);
			MMOCore.plugin.skillManager.getAll().forEach(skill -> data.lockSkill(skill));
			while (data.hasSkillBound(0))
				data.unbindSkill(0);
			sender.sendMessage(ChatColor.GOLD + player.getName() + ChatColor.YELLOW + "'s skill data was succesfully reset.");
			return CommandResult.SUCCESS;
		}
	}

	public class ResetLevelsCommandMap extends CommandEnd {
		public ResetLevelsCommandMap(CommandMap parent) {
			super(parent, "levels");

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

			PlayerData data = PlayerData.get(player);
			data.setLevel(1);
			data.setExperience(0);
			for (Profession profession : MMOCore.plugin.professionManager.getAll()) {
				data.getCollectionSkills().setExperience(profession, 0);
				data.getCollectionSkills().setLevel(profession, 0);
			}
			sender.sendMessage(ChatColor.GOLD + player.getName() + ChatColor.YELLOW + "'s levels were succesfully reset.");

			return CommandResult.SUCCESS;
		}
	}
}
