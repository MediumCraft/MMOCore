package net.Indyuce.mmocore.api.quest.trigger;

import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.Bukkit;

public abstract class Trigger {

	public static String TRIGGER_PREFIX = "mmocore_trigger";
	private final long delay;

	public Trigger(MMOLineConfig config) {
		delay = config.contains("delay") ? (long) (config.getDouble("delay") * 20.) : 0;
	}

	public boolean hasDelay() {
		return delay > 0;
	}

	public long getDelay() {
		return delay;
	}

	public void schedule(PlayerData player) {
		if (delay <= 0)
			apply(player);
		else
			Bukkit.getScheduler().scheduleSyncDelayedTask(MMOCore.plugin, () -> apply(player), delay);
	}

	/**
	 * This method must not be used directly when executing triggers after quest
	 * objectives for example, because this method does NOT take into account
	 * trigger delay, {@link #schedule(PlayerData)} is used instead.
	 */
	public abstract void apply(PlayerData player);
}
