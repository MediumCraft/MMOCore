package net.Indyuce.mmocore.command.rpg;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.Indyuce.mmocore.api.util.item.CurrencyItem;
import net.Indyuce.mmocore.command.api.CommandEnd;
import net.Indyuce.mmocore.command.api.CommandMap;
import net.Indyuce.mmocore.command.api.Parameter;
import net.mmogroup.mmolib.api.util.SmartGive;

public class NoteCommandMap extends CommandEnd {
	public NoteCommandMap(CommandMap parent) {
		super(parent, "note");

		addParameter(Parameter.PLAYER);
		addParameter(new Parameter("<worth>", (list) -> list.addAll(Arrays.asList("10", "20", "30", "40", "50", "60", "70", "80", "90", "100"))));
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		if (args.length < 3)
			return CommandResult.THROW_USAGE;

		Player player = Bukkit.getPlayer(args[1]);
		if (player == null) {
			sender.sendMessage(ChatColor.RED + "Could not find the player called " + args[1] + ".");
			return CommandResult.FAILURE;
		}

		int worth;
		try {
			worth = Integer.parseInt(args[2]);
		} catch (NumberFormatException exception) {
			sender.sendMessage(ChatColor.RED + args[2] + " is not a valid number.");
			return CommandResult.FAILURE;
		}

		new SmartGive(player).give(new CurrencyItem("NOTE", worth).build());
		return CommandResult.SUCCESS;
	}

}
