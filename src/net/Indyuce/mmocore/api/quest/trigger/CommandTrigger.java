package net.Indyuce.mmocore.api.quest.trigger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.load.MMOLineConfig;
import net.Indyuce.mmocore.api.player.PlayerData;

public class CommandTrigger extends Trigger {
	private final String command;

	public CommandTrigger(MMOLineConfig config) {
		super(config);

		config.validate("format");
		command = config.getString("format");
	}

	@Override
	public void apply(PlayerData player) {
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), format(player.getPlayer()));
	}

	private String format(Player player) {
		return MMOCore.plugin.placeholderParser.parse(player, command.replace("%player%", player.getName()));
	}
}
