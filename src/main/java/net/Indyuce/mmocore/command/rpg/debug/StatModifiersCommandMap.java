package net.Indyuce.mmocore.command.rpg.debug;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.stats.StatType;
import net.Indyuce.mmocore.command.api.CommandEnd;
import net.Indyuce.mmocore.command.api.CommandMap;
import net.Indyuce.mmocore.command.api.Parameter;
import net.mmogroup.mmolib.api.stat.StatInstance;
import net.mmogroup.mmolib.api.stat.modifier.StatModifier;

public class StatModifiersCommandMap extends CommandEnd {
	public StatModifiersCommandMap(CommandMap parent) {
		super(parent, "statmods");

		addParameter(new Parameter("<stat>", list -> {
			for (StatType stat : StatType.values())
				list.add(stat.name());
		}));
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		if (args.length < 3)
			return CommandResult.THROW_USAGE;

		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "This command can only be used by a player.");
			return CommandResult.FAILURE;
		}
		PlayerData data = PlayerData.get((Player) sender);

		StatType stat;
		try {
			stat = StatType.valueOf(args[2].toUpperCase().replace("-", "_").replace(" ", "_"));
		} catch (IllegalArgumentException exception) {
			sender.sendMessage(ChatColor.RED + "Could not find stat: " + args[2] + ".");
			return CommandResult.FAILURE;
		}

		StatInstance instance = data.getStats().getInstance(stat);
		sender.sendMessage("Stat Modifiers (" + instance.getKeys().size() + "):");
		for (String key : instance.getKeys()) {
			StatModifier mod = instance.getAttribute(key);
			sender.sendMessage("- " + key + ": " + mod.getValue() + " " + mod.getType().name());
		}

		return CommandResult.SUCCESS;
	}
}
