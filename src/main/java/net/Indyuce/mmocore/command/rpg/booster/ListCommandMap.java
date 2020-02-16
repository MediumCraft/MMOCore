package net.Indyuce.mmocore.command.rpg.booster;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.experience.Booster;
import net.Indyuce.mmocore.api.util.math.format.DelayFormat;
import net.Indyuce.mmocore.command.api.CommandEnd;
import net.Indyuce.mmocore.command.api.CommandMap;
import net.mmogroup.mmolib.MMOLib;

public class ListCommandMap extends CommandEnd {
	public ListCommandMap(CommandMap parent) {
		super(parent, "list");
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player))
			return CommandResult.FAILURE;

		sender.sendMessage(ChatColor.YELLOW + "----------------------------------------------------");
		for (Booster booster : MMOCore.plugin.boosterManager.getBoosters())
			if (!booster.isTimedOut())
				MMOLib.plugin.getNMS().sendJson((Player) sender, "{\"text\":\"" + ChatColor.YELLOW + "- " + ChatColor.GOLD + MMOCore.plugin.configManager.decimal.format((1 + booster.getExtra())) + "x" + ChatColor.YELLOW + " Booster - " + ChatColor.GOLD + (!booster.hasProfession() ? "ExploreAttributesCommand" : booster.getProfession().getName()) + ChatColor.YELLOW + " - " + ChatColor.GOLD + new DelayFormat().format(booster.getCreationDate() + booster.getLength() - System.currentTimeMillis()) + "\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"/mmocore booster remove " + booster.getUniqueId().toString() + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"Click to remove.\"}}}");
		sender.sendMessage(ChatColor.YELLOW + "----------------------------------------------------");

		return CommandResult.SUCCESS;
	}
}
