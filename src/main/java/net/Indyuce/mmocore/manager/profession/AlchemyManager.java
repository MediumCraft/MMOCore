package net.Indyuce.mmocore.manager.profession;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.potion.PotionType;

import net.Indyuce.mmocore.manager.MMOManager;

public class AlchemyManager implements MMOManager {
	public double splash, lingering, upgrade, extend;

	// private Map<PotionEffectType, Double> custom = new HashMap<>();
	private final Map<PotionType, Double> base = new HashMap<>();

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
	public void reload() {
		// TODO Auto-generated method stub

	}

	@Override
	public void clear() {
		splash = lingering = upgrade = extend = 1;
		base.clear();
	}
}
