package net.Indyuce.mmocore.command.rpg.booster;

import org.bukkit.command.CommandSender;

import net.Indyuce.mmocore.command.api.CommandMap;

public class BoosterCommandMap extends CommandMap {
	public BoosterCommandMap(CommandMap parent) {
		super(parent, "booster");

		addFloor(new CreateCommandMap(this));
		addFloor(new ListCommandMap(this));
		addFloor(new RemoveCommandMap(this));
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		return CommandResult.THROW_USAGE;
	}
}
