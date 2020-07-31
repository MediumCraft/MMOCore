package net.Indyuce.mmocore.api.quest.trigger;

import org.bukkit.Bukkit;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.mmogroup.mmolib.api.MMOLineConfig;

public abstract class Trigger {
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

	/*
	 * this method must not be used directly when executing triggers after quest
	 * objectives for example, because this method does NOT take into account
	 * trigger delay
	 */
	public abstract void apply(PlayerData player);
}
