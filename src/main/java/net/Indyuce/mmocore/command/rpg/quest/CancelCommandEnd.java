package net.Indyuce.mmocore.command.rpg.quest;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.quest.PlayerQuests;
import net.Indyuce.mmocore.command.api.CommandEnd;
import net.Indyuce.mmocore.command.api.CommandMap;
import net.Indyuce.mmocore.command.api.Parameter;

public class CancelCommandEnd extends CommandEnd {
	public CancelCommandEnd(CommandMap parent) {
		super(parent, "cancel");

		addParameter(Parameter.PLAYER);
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		if (args.length < 3)
			return CommandResult.THROW_USAGE;

		Player player = Bukkit.getPlayer(args[2]);
		if (player == null) {
			sender.sendMessage(ChatColor.RED + "Could not find player called " + args[2] + ".");
			return CommandResult.FAILURE;
		}

		PlayerQuests quests = PlayerData.get(player).getQuestData();
		if (!quests.hasCurrent()) {
			if (sender instanceof Player)
				sender.sendMessage(ChatColor.RED + player.getName() + " has no ongoing quest.");
			return CommandResult.SUCCESS;
		}

		quests.start(null);
		if (sender instanceof Player)
			sender.sendMessage(ChatColor.YELLOW + player.getName() + " no longer has any ongoing quest.");
		return CommandResult.SUCCESS;
	}
}
