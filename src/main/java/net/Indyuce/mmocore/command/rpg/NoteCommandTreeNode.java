package net.Indyuce.mmocore.command.rpg;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.Indyuce.mmocore.api.util.item.CurrencyItemBuilder;
import io.lumine.mythic.lib.api.util.SmartGive;
import io.lumine.mythic.lib.command.api.CommandTreeNode;
import io.lumine.mythic.lib.command.api.Parameter;

public class NoteCommandTreeNode extends CommandTreeNode {
	public NoteCommandTreeNode(CommandTreeNode parent) {
		super(parent, "note");

		addParameter(Parameter.PLAYER);
		addParameter(new Parameter("<worth>",
				(explorer, list) -> list.addAll(Arrays.asList("10", "20", "30", "40", "50", "60", "70", "80", "90", "100"))));
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

		new SmartGive(player).give(new CurrencyItemBuilder("NOTE", worth).build());
		return CommandResult.SUCCESS;
	}
}
