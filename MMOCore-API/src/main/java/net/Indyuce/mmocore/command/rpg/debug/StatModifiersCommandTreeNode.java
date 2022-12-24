package net.Indyuce.mmocore.command.rpg.debug;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.stat.StatInstance;
import io.lumine.mythic.lib.api.stat.modifier.StatModifier;
import io.lumine.mythic.lib.command.api.CommandTreeNode;
import io.lumine.mythic.lib.command.api.Parameter;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class StatModifiersCommandTreeNode extends CommandTreeNode {
	public StatModifiersCommandTreeNode(CommandTreeNode parent) {
		super(parent, "statmods");

        addParameter(new Parameter("<stat>", (explorer, list) -> list.add("STAT_ID")));
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

		StatInstance instance = data.getMMOPlayerData().getStatMap().getInstance(UtilityMethods.enumName(args[2]));
		sender.sendMessage("Stat Modifiers (" + instance.getKeys().size() + "):");
		for (String key : instance.getKeys()) {
			StatModifier mod = instance.getModifier(key);
			sender.sendMessage("- " + key + ": " + mod.getValue() + " " + mod.getType().name());
		}

		return CommandResult.SUCCESS;
	}
}
