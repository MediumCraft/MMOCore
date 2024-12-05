package net.Indyuce.mmocore.api.quest.trigger;

import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.version.Sounds;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.Sound;

public class SoundTrigger extends Trigger {
	private final Sound sound;
	private final float vol, pitch;

	public SoundTrigger(MMOLineConfig config) {
		super(config);

		config.validate("sound");

		sound = Sounds.fromName(config.getString("sound").toUpperCase().replace("-", "_"));
		vol = config.contains("volume") ? (float) config.getDouble("volume") : 1f;
		pitch = config.contains("pitch") ? (float) config.getDouble("pitch") : 1f;
	}

	@Override
	public void apply(PlayerData player) {
		if(!player.isOnline()) return;
		player.getPlayer().playSound(player.getPlayer().getLocation(), sound, vol, pitch);
	}
}
