package net.Indyuce.mmocore.command.api;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public abstract class CommandRoot extends CommandMap {
	public CommandRoot(String id) {
		super(null, id);
	}

	public void executeCommand(CommandSender sender, String[] args) {
		CommandMap read = readCommand(args).read();

		if (read.execute(sender, args) == CommandResult.THROW_USAGE)
			read.calculateUsageList().forEach(str -> sender.sendMessage(ChatColor.YELLOW + "/" + str));
	}

	public CommandParser readCommand(String[] args) {
		return new CommandParser(this, args);
	}

	public class CommandParser {
		private CommandMap current;
		private int parameter = 0;

		/*
		 * used to parse a command and identify the commandMap which is supposed
		 * to
		 */
		public CommandParser(CommandRoot begin, String[] args) {
			this.current = begin;

			for (String arg : args)

				/*
				 * check if current command floor has the corresponding arg, if
				 * so let the next floor handle the command.
				 */
				if (parameter == 0 && current.hasFloor(arg))
					current = current.getFloor(arg);

				/*
				 * if the plugin cannot find a command map higher, then the
				 * current floor will handle the command.
				 */
				else
					parameter++;
		}

		public CommandMap read() {
			return current;
		}

		public int extraCount() {
			return parameter;
		}

		public List<String> readTabCompletion() {
			return current.calculateTabCompletion(Math.max(0, parameter - 1));
		}
	}
}
