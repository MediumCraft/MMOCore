package net.Indyuce.mmocore.command.rpg.debug;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.stats.StatType;
import net.Indyuce.mmocore.command.api.CommandEnd;
import net.Indyuce.mmocore.command.api.CommandMap;
import net.Indyuce.mmocore.command.api.Parameter;

public class StatValueCommandMap extends CommandEnd {
	public StatValueCommandMap(CommandMap parent) {
		super(parent, "statvalue");

		addParameter(new Parameter("<stat>", list -> { for(StatType stat : StatType.values()) list.add(stat.name()); }));
		addParameter(new Parameter("(formatted)", list -> { list.add("true"); }));
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		if (args.length < 3 )
			return CommandResult.THROW_USAGE;

		if(!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "This command can only be used by a player.");
			return CommandResult.FAILURE;
		}
		PlayerData data = PlayerData.get((Player) sender);
		
		StatType stat = StatType.valueOf(args[2]);
		if (stat == null) {
			sender.sendMessage(ChatColor.RED + "Could not find stat: " + args[2] + ".");
			return CommandResult.FAILURE;
		}
		
		if(args.length > 3 && args[3].equals("true"))
			sender.sendMessage(DebugCommandMap.commandPrefix + "Stat Value (" + ChatColor.BLUE + stat.name() + ChatColor.WHITE  + "): " + ChatColor.GREEN + stat.format(data.getStats().getStat(stat)) + ChatColor.WHITE + " *");
		else sender.sendMessage(DebugCommandMap.commandPrefix + "Stat Value (" + ChatColor.BLUE + stat.name() + ChatColor.WHITE + "): " + ChatColor.GREEN + data.getStats().getStat(stat));
		
		return CommandResult.SUCCESS;
	}
}
