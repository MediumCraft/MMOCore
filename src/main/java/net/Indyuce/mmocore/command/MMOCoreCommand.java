package net.Indyuce.mmocore.command;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import net.Indyuce.mmocore.command.api.CommandRoot;
import net.Indyuce.mmocore.command.rpg.CoinsCommandMap;
import net.Indyuce.mmocore.command.rpg.NoteCommandMap;
import net.Indyuce.mmocore.command.rpg.ReloadCommandMap;
import net.Indyuce.mmocore.command.rpg.admin.AdminCommandMap;
import net.Indyuce.mmocore.command.rpg.booster.BoosterCommandMap;
import net.Indyuce.mmocore.command.rpg.debug.DebugCommandMap;
import net.Indyuce.mmocore.command.rpg.waypoint.WaypointsCommandMap;

public class MMOCoreCommand extends CommandRoot implements CommandExecutor, TabCompleter {
	public MMOCoreCommand() {
		super("mmocore");

		addFloor(new ReloadCommandMap(this));
		addFloor(new CoinsCommandMap(this));
		addFloor(new NoteCommandMap(this));
		addFloor(new AdminCommandMap(this));
		addFloor(new DebugCommandMap(this));
		addFloor(new BoosterCommandMap(this));
		addFloor(new WaypointsCommandMap(this));
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		return CommandResult.THROW_USAGE;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!sender.hasPermission("mmocore.admin"))
			return false;

		executeCommand(sender, args);
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		if (!sender.hasPermission("mmocore.admin"))
			return new ArrayList<>();

		CommandReader reader = readCommand(args);
		List<String> list = reader.readTabCompletion();
		return args[args.length - 1].isEmpty() ? list : list.stream().filter(string -> string.toLowerCase().startsWith(args[args.length - 1].toLowerCase())).collect(Collectors.toList());
	}
}
