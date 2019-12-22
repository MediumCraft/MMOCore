package net.Indyuce.mmocore.api.player.stats.stat.modifier;

import org.bukkit.scheduler.BukkitRunnable;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.stats.PlayerStats.StatInstance;

public class TemporaryStatModifier extends StatModifier {
	private final BukkitRunnable runnable;
	
	public TemporaryStatModifier(double d, long duration, boolean relative, String key, StatInstance ins) {
		super(d, relative);

		(runnable = new BukkitRunnable() {
			public void run() {
				ins.remove(key);
			}
		}).runTaskLater(MMOCore.plugin, duration);
	}

	public void close() {
		if (!runnable.isCancelled())
			runnable.cancel();
	}
}
