package net.Indyuce.mmocore.api.loot;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;

import net.Indyuce.mmocore.api.util.math.particle.ChestParticleEffect;

public class ChestTier {
	private final TierEffect effect;
	private final int weight;

	public ChestTier(ConfigurationSection config) {
		effect = config.isConfigurationSection("effect") ? new TierEffect(config.getConfigurationSection("effect"))
				: null;
		weight = config.getInt("weight", 1);
	}
	
	public int getWeight() {
		return weight;
	}

	public boolean hasEffect() {
		return effect != null;
	}

	public TierEffect getEffect() {
		return effect;
	}

	public class TierEffect {
		private final ChestParticleEffect type;
		private final Particle particle;

		public TierEffect(ConfigurationSection config) {
			Validate.notNull(config, "Could not load tier config");
			type = ChestParticleEffect
					.valueOf(config.getString("type", "OFFSET").toUpperCase().replace("-", "_").replace(" ", "_"));
			particle = Particle
					.valueOf(config.getString("particle", "FLAME").toUpperCase().replace("-", "_").replace(" ", "_"));
		}

		public void play(Location loc) {
			type.play(loc, particle);
		}
	}
}
