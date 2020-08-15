package net.Indyuce.mmocore.command.rpg.quest;

import org.bukkit.command.CommandSender;

import net.mmogroup.mmolib.command.api.CommandTreeNode;

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
