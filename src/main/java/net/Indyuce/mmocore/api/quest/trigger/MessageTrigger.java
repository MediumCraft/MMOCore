package net.Indyuce.mmocore.api.quest.trigger;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.load.MMOLineConfig;
import net.Indyuce.mmocore.api.player.PlayerData;

public class MessageTrigger extends Trigger {
	private String message;

	public MessageTrigger(MMOLineConfig config) {
		super(config);

		config.validate("format");
		message = config.getString("format");
	}

	@Override
	public void apply(PlayerData player) {
		player.getPlayer().sendMessage(format(player.getPlayer()));
	}

	private String format(Player player) {
		message = ChatColor.translateAlternateColorCodes('&', message);
		return MMOCore.plugin.placeholderParser.parse(player, message.replace("%player%", player.getName()));
	}
}
