package net.Indyuce.mmocore.command.rpg.booster;

import java.util.Iterator;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.experience.Booster;
import net.Indyuce.mmocore.command.api.CommandEnd;
import net.Indyuce.mmocore.command.api.CommandMap;
import net.Indyuce.mmocore.command.api.Parameter;

public class RemoveCommandMap extends CommandEnd {
	public RemoveCommandMap(CommandMap parent) {
		super(parent, "remove");

		addParameter(new Parameter("<id>", (list) -> MMOCore.plugin.boosterManager.getBoosters().forEach(booster -> list.add("" + booster.getUniqueId().toString()))));
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		if (args.length < 3)
			return CommandResult.THROW_USAGE;

		UUID uuid;
		try {
			uuid = UUID.fromString(args[2]);
		} catch (IllegalArgumentException exception) {
			sender.sendMessage(ChatColor.RED + "Couldn't load ID " + args[2] + ".");
			return CommandResult.FAILURE;
		}

		for (Iterator<Booster> iterator = MMOCore.plugin.boosterManager.getBoosters().iterator(); iterator.hasNext();) {
			Booster booster = iterator.next();
			if (booster.getUniqueId().equals(uuid)) {
				iterator.remove();
				sender.sendMessage(ChatColor.YELLOW + "Successfully unregistered this booster.");
				break;
			}
		}
		return CommandResult.SUCCESS;
	}
}
