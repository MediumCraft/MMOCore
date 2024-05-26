package net.Indyuce.mmocore.api.quest.trigger;

import io.lumine.mythic.lib.util.annotation.BackwardsCompatibility;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.entity.Player;

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

	@BackwardsCompatibility(version = "1.12-SNAPSHOT")
	private String format(Player player) {
		// TODO remove use of confusing non-PAPI %player% placeholder
		return MMOCore.plugin.placeholderParser.parse(player, message.replace("%player%", player.getName()));
	}
}
