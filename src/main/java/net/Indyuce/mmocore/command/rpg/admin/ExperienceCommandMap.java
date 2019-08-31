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

public class ExperienceCommandMap extends CommandEnd {
	public ExperienceCommandMap(CommandMap parent) {
		super(parent, "exp");

		addParameter(Parameter.PLAYER);
		addParameter(Parameter.PROFESSION);
		addParameter(Parameter.AMOUNT);
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		if (args.length < 5)
			return CommandResult.THROW_USAGE;

		Player player = Bukkit.getPlayer(args[2]);
		if (player == null) {
			sender.sendMessage(ChatColor.RED + "Could not find the player called " + args[2] + ".");
			return CommandResult.FAILURE;
		}

		int amount;
		try {
			amount = Integer.parseInt(args[4]);
		} catch (Exception e) {
			sender.sendMessage(ChatColor.RED + args[4] + " is not a valid number.");
			return CommandResult.FAILURE;
		}

		if (args[3].equalsIgnoreCase("main")) {
			PlayerData data = PlayerData.get(player);
			data.giveExperience(amount);
			sender.sendMessage(ChatColor.GOLD + player.getName() + ChatColor.YELLOW + " now has " + ChatColor.GOLD + data.getExperience() + ChatColor.YELLOW + " EXP.");
			return CommandResult.SUCCESS;
		}

		String format = args[3].toLowerCase().replace("_", "-");
		if (!MMOCore.plugin.professionManager.has(format)) {
			sender.sendMessage(ChatColor.RED + format + " is not a valid profession.");
			return CommandResult.FAILURE;
		}

		Profession profession = MMOCore.plugin.professionManager.get(format);
		PlayerData data = PlayerData.get(player);
		data.getCollectionSkills().giveExperience(profession, amount);
		sender.sendMessage(ChatColor.GOLD + player.getName() + ChatColor.YELLOW + " now has " + ChatColor.GOLD + data.getCollectionSkills().getExperience(profession) + ChatColor.YELLOW + " EXP in " + profession.getName() + ".");
		return CommandResult.SUCCESS;
	}
}
