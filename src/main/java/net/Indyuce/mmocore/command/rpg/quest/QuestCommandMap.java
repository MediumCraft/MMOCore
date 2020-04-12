package net.Indyuce.mmocore.command.rpg.quest;

import org.bukkit.command.CommandSender;

import net.Indyuce.mmocore.command.api.CommandMap;

public class QuestCommandMap extends CommandMap {

	public QuestCommandMap(CommandMap parent) {
		super(parent, "quest");

		addFloor(new StartCommandEnd(this));
		addFloor(new CancelCommandEnd(this));
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		return CommandResult.THROW_USAGE;
	}
}
