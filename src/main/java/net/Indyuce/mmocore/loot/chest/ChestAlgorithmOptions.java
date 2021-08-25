package net.Indyuce.mmocore.loot.chest;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;

public class ChestAlgorithmOptions {

	/*
	 * min and max range represents the range at which the chest can spawn
	 * around the player. height is the Z delta in which the chest can spawn,
	 * relative to the player's altitude
	 */
	public final double minRange, maxRange, height;

	/*
	 * maximum amount of trials the algorithm will run in order to find a
	 * suitable location for a chest around the player.
	 */
	public final int iterations;

	public static final ChestAlgorithmOptions DEFAULT = new ChestAlgorithmOptions(10, 30, 8, 15);

	/*
	 * this is purely to let server owners tweak the chest random location
	 * finder algorithm.
	 */
	public ChestAlgorithmOptions(ConfigurationSection config) {
		Validate.notNull(config, "Config cannot be null");

		minRange = config.getDouble("min-range", DEFAULT.minRange);
		maxRange = config.getDouble("max-range", DEFAULT.maxRange);
		height = config.getDouble("height", DEFAULT.height);
		iterations = config.getInt("iterations", DEFAULT.iterations);

		Validate.isTrue(minRange < maxRange, "Max range must be greater than min range");
		Validate.isTrue(height > 0, "Height must be strictly positive");
		Validate.isTrue(iterations > 0, "Iterations must be strictly positive");
	}

	/*
	 * can be used to register loot chest regions with external plugins, and
	 * used by the default alg options instance
	 */
	public ChestAlgorithmOptions(double minRange, double maxRange, double height, int iterations) {
		this.minRange = minRange;
		this.maxRange = maxRange;
		this.height = height;
		this.iterations = iterations;
	}
}
