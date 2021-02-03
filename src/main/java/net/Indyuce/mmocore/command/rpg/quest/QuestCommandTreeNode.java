package net.Indyuce.mmocore.command.rpg.quest;

import org.bukkit.command.CommandSender;

import io.lumine.mythic.lib.mmolibcommands.api.CommandTreeNode;

public class QuestCommandTreeNode extends CommandTreeNode {

	public QuestCommandTreeNode(CommandTreeNode parent) {
		super(parent, "quest");

		addChild(new StartCommandTreeNode(this));
		addChild(new CancelCommandTreeNode(this));
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		return CommandResult.THROW_USAGE;
	}
}
