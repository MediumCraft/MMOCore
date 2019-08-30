package net.Indyuce.mmocore.api.math.particle;

import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.scheduler.BukkitRunnable;

import net.Indyuce.mmocore.MMOCore;

public class PotionParticles extends BukkitRunnable {
	private double r, g, b;
	private ThrownPotion potion;
	private boolean valid = true;

	public PotionParticles(ThrownPotion potion) {
		this.potion = potion;

		Color color = ((PotionMeta) potion.getItem().getItemMeta()).getColor();
		if (color == null) {
			valid = false;
			return;
		}

		r = Math.max((double) 1 / 255, ratio(color.getRed()));
		g = ratio(color.getGreen());
		b = ratio(color.getBlue());

	}

	public void start() {
		if (valid)
			runTaskTimer(MMOCore.plugin, 0, 1);
	}

	private double ratio(int l) {
		return (double) l / 255.;
	}

	@Override
	public void run() {
		if (potion == null || potion.isDead()) {
			cancel();
			return;
		}

		potion.getWorld().spawnParticle(Particle.SPELL_MOB, potion.getLocation(), 0, r, g, b);
	}
}
