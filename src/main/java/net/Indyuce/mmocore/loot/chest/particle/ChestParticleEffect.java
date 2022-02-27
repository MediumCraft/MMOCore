package net.Indyuce.mmocore.loot.chest.particle;

import java.util.function.BiConsumer;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.scheduler.BukkitRunnable;

import net.Indyuce.mmocore.MMOCore;

public enum ChestParticleEffect {

	HELIX((loc, particle) -> {
		new BukkitRunnable() {
			double ti = 0;

			public void run() {
				if ((ti += Math.PI / 16) > Math.PI * 2)
					cancel();
				for (double j = 0; j < Math.PI * 2; j += Math.PI * 2 / 5)
					loc.getWorld().spawnParticle(particle, loc.clone().add(Math.cos(j + ti / 2), -.5 + ti / Math.PI / 2, Math.sin(j + ti / 2)), 0);
			}
		}.runTaskTimer(MMOCore.plugin, 0, 1);
	}),

	OFFSET((loc, particle) -> {
		new BukkitRunnable() {
			int ti = 0;

			public void run() {
				if (ti++ > 20)
					cancel();
				for (double j = 0; j < Math.PI * 2; j += Math.PI * 2 / 5)
					loc.getWorld().spawnParticle(particle, loc.clone(), 1, .5, .5, .5, 0);
			}
		}.runTaskTimer(MMOCore.plugin, 0, 1);
	}),

	GALAXY((loc, particle) -> {
		new BukkitRunnable() {
			double ti = 0;

			public void run() {
				if ((ti += Math.PI / 16) > Math.PI * 2)
					cancel();
				for (double j = 0; j < Math.PI * 2; j += Math.PI * 2 / 5)
					loc.getWorld().spawnParticle(particle, loc.clone().add(0, -.1, 0), 0, Math.cos(j + ti / 2), 0, Math.sin(j + ti / 2), .13);
			}
		}.runTaskTimer(MMOCore.plugin, 0, 1);
	});

	private final BiConsumer<Location, Particle> func;

	ChestParticleEffect(BiConsumer<Location, Particle> func) {
		this.func = func;
	}

	public void play(Location loc, Particle particle) {
		func.accept(loc, particle);
	}
}
