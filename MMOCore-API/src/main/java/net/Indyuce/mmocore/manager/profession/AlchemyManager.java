package net.Indyuce.mmocore.manager.profession;

import net.Indyuce.mmocore.MMOCore;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.potion.PotionType;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class AlchemyManager extends SpecificProfessionManager {
	public double splash, lingering, upgrade, extend;

	// private Map<PotionEffectType, Double> custom = new HashMap<>();
	private final Map<PotionType, Double> base = new HashMap<>();

	public AlchemyManager() {
		super("alchemy-experience");
	}

	@Override
	public void loadProfessionConfiguration(ConfigurationSection config) {
		splash = 1 + config.getDouble("special.splash") / 100;
		lingering = 1 + config.getDouble("special.lingering") / 100;
		extend = 1 + config.getDouble("special.extend") / 100;
		upgrade = 1 + config.getDouble("special.upgrade") / 100;

		for (String key : config.getConfigurationSection("effects").getKeys(false))
			try {
				PotionType type = PotionType.valueOf(key.toUpperCase().replace("-", "_").replace(" ", "_"));
				MMOCore.plugin.alchemyManager.registerBaseExperience(type, config.getDouble("effects." + key));
			} catch (IllegalArgumentException exception) {
				MMOCore.log(Level.WARNING, "Could not read potion type from " + key);
			}
	}

	// public double getWeight(PotionEffectType type) {
	// return custom.containsKey(type) ? custom.get(type) : 0;
	// }

	// public void registerEffectWeight(PotionEffectType type, double weight) {
	// custom.put(type, weight);
	// }

	public void registerBaseExperience(PotionType type, double value) {
		base.put(type, value);
	}

	public double getBaseExperience(PotionType type) {
		return base.get(type);
	}

	// public class BaseExperience {
	// private final double normal, extend, upgrade;
	//
	// public BaseExperience(double normal, double extend, double upgrade) {
	// this.extend = extend;
	// this.upgrade = upgrade;
	// this.normal = normal;
	// }
	// }

    @Override
    public void initialize(boolean clearBefore) {
        if (clearBefore) {
            splash = lingering = upgrade = extend = 1;
            base.clear();
        }
    }
}
