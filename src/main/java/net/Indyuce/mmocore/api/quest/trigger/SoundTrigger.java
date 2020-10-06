package net.Indyuce.mmocore.api.quest.trigger;

import org.bukkit.Sound;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.mmogroup.mmolib.api.MMOLineConfig;

public class SoundTrigger extends Trigger {
	private final Sound sound;
	private final float vol, pitch;

	public SoundTrigger(MMOLineConfig config) {
		super(config);

		config.validate("sound");

		sound = Sound.valueOf(config.getString("sound").toUpperCase().replace("-", "_"));
		vol = config.contains("volume") ? (float) config.getDouble("volume") : 1f;
		pitch = config.contains("pitch") ? (float) config.getDouble("pitch") : 1f;
	}

	@Override
	public void apply(PlayerData player) {
		if(!player.isOnline()) return;
		player.getPlayer().playSound(player.getPlayer().getLocation(), sound, vol, pitch);
	}
}
