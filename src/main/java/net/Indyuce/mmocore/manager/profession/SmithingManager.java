package net.Indyuce.mmocore.manager.profession;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;

import net.Indyuce.mmocore.manager.MMOManager;

public class SmithingManager implements MMOManager {
	private final Map<Material, Double> base = new HashMap<>();

	public void registerBaseExperience(Material material, double value) {
		base.put(material, value);
	}

	public double getBaseExperience(Material material) {
		return base.get(material);
	}
	
	public boolean hasExperience(Material material) {
		return base.containsKey(material);
	}

	@Override
	public void reload() {
		// TODO Auto-generated method stub

	}

	@Override
	public void clear() {
		base.clear();
	}
}
