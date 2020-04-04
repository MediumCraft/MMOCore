package net.Indyuce.mmocore.api.quest.trigger;

import org.bukkit.Bukkit;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.mmogroup.mmolib.api.MMOLineConfig;

public abstract class Trigger {
	private final long delay;

	public Trigger(MMOLineConfig config) {
		delay = config.contains("delay") ? (long) (config.getDouble("delay") * 20) : 0;
	}

	public boolean hasDelay() {
		return delay > 0;
	}

	public long getDelay() {
		return delay;
	}

	public void schedule(PlayerData player) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(MMOCore.plugin, () -> apply(player), delay);
	}

	public abstract void apply(PlayerData player);
}
