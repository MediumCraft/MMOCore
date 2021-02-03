package net.Indyuce.mmocore.api.quest.trigger;

import org.bukkit.entity.Player;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import io.lumine.mythic.lib.api.MMOLineConfig;

public class MessageTrigger extends Trigger {
	private final String message;

	public MessageTrigger(MMOLineConfig config) {
		super(config);

		config.validate("format");
		message = config.getString("format");
	}

	@Override
	public void apply(PlayerData player) {
		if(!player.isOnline()) return;
		player.getPlayer().sendMessage(format(player.getPlayer()));
	}

	private String format(Player player) {
		return MMOCore.plugin.placeholderParser.parse(player, message.replace("%player%", player.getName()));
	}
}
