package net.Indyuce.mmocore.command.rpg.admin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.PlayerClass;
import net.Indyuce.mmocore.command.api.CommandEnd;
import net.Indyuce.mmocore.command.api.CommandMap;
import net.Indyuce.mmocore.command.api.Parameter;

public class ClassCommandMap extends CommandEnd {
	public ClassCommandMap(CommandMap parent) {
		super(parent, "class");

		addParameter(Parameter.PLAYER);
		addParameter(new Parameter("<class>", (list) -> MMOCore.plugin.classManager.getAll().forEach(profess -> list.add(profess.getId()))));
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		if (args.length < 4)
			return CommandResult.THROW_USAGE;

		Player player = Bukkit.getPlayer(args[2]);
		if (player == null) {
			sender.sendMessage(ChatColor.RED + "Could not find the player called " + args[2] + ".");
			return CommandResult.FAILURE;
		}

		String format = args[3].toUpperCase().replace("-", "_");
		if (!MMOCore.plugin.classManager.has(format)) {
			sender.sendMessage(ChatColor.RED + "Could not find class " + format + ".");
			return CommandResult.FAILURE;
		}

		PlayerClass profess = MMOCore.plugin.classManager.get(format);

		PlayerData data = PlayerData.get(player);
		data.setClass(profess);
		sender.sendMessage(ChatColor.GOLD + player.getName() + ChatColor.YELLOW + " is now a " + ChatColor.GOLD + profess.getName() + ChatColor.YELLOW + ".");
		return CommandResult.SUCCESS;
	}
}
