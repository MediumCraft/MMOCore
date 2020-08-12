package net.Indyuce.mmocore.api.util.math.particle;

import java.util.function.Consumer;

import org.apache.commons.lang.Validate;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;

public class CastingParticle {
	private final Consumer<Location> display;

	public CastingParticle(ConfigurationSection config) {
		Validate.notNull(config, "Casting particle config cannot be null");

		String format = config.getString("particle");
		Validate.notNull(format, "Could not read particle name");
		Particle particle = Particle.valueOf(format.toUpperCase().replace("-", "_").replace(" ", "_"));

		if (config.contains("color")) {
			final float size = (float) config.getDouble("size") == 0 ? 1 : (float) Math.max(config.getDouble("size"), 0);
			Color color = Color.fromRGB(config.getInt("color.red"), config.getInt("color.green"), config.getInt("color.blue"));

			display = (loc) -> loc.getWorld().spawnParticle(particle, loc, 1, new Particle.DustOptions(color, size));
			return;
		}

		if (config.contains("material")) {
			format = config.getString("material");
			Validate.notNull(format, "Could not read material name");
			Material material = Material.valueOf(format.toUpperCase().replace("-", "_").replace(" ", "_"));

			display = (loc) -> loc.getWorld().spawnParticle(particle, loc, 1, material.createBlockData());
			return;
		}

		display = (loc) -> loc.getWorld().spawnParticle(particle, loc, 0);
	}
	
	public CastingParticle(Particle particle) {
		display = (loc) -> loc.getWorld().spawnParticle(particle, loc, 0);
	}

	public void display(Location loc) {
		display.accept(loc);
	}
}
