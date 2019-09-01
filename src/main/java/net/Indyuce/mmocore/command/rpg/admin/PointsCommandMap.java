package net.Indyuce.mmocore.command.rpg.admin;

import java.util.function.BiConsumer;
import java.util.function.Function;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.command.api.CommandEnd;
import net.Indyuce.mmocore.command.api.CommandMap;
import net.Indyuce.mmocore.command.api.Parameter;

public class PointsCommandMap extends CommandMap {
	private final String type;
	private final Function<PlayerData, Integer> get;

	public PointsCommandMap(String type, CommandMap parent, BiConsumer<PlayerData, Integer> set, BiConsumer<PlayerData, Integer> give, Function<PlayerData, Integer> get) {
		super(parent, type + "-points");

		this.type = type;
		this.get = get;

		addFloor(new ActionCommandMap(this, "set", set));
		addFloor(new ActionCommandMap(this, "give", give));
	}

	public class ActionCommandMap extends CommandEnd {
		private final BiConsumer<PlayerData, Integer> action;

		public ActionCommandMap(CommandMap parent, String type, BiConsumer<PlayerData, Integer> action) {
			super(parent, type);

			this.action = action;

			addParameter(Parameter.PLAYER);
			addParameter(Parameter.AMOUNT);
		}

		@Override
		public CommandResult execute(CommandSender sender, String[] args) {
			if (args.length < 5)
				return CommandResult.THROW_USAGE;

			Player player = Bukkit.getPlayer(args[3]);
			if (player == null) {
				sender.sendMessage(ChatColor.RED + "Could not find the player called " + args[2] + ".");
				return CommandResult.FAILURE;
			}

			int amount = 0;
			try {
				amount = Integer.parseInt(args[4]);
			} catch (Exception e) {
				sender.sendMessage(ChatColor.RED + args[4] + " is not a valid number.");
				return CommandResult.FAILURE;
			}

			PlayerData data = PlayerData.get(player);
			action.accept(data, amount);
			sender.sendMessage(ChatColor.GOLD + player.getName() + ChatColor.YELLOW + " now has " + ChatColor.GOLD + get.apply(data) + ChatColor.YELLOW + " " + type + " points.");
			return CommandResult.SUCCESS;
		}
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		return CommandResult.THROW_USAGE;
	}
}
