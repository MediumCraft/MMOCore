package net.Indyuce.mmocore.manager.profession;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.enchantments.Enchantment;

import net.Indyuce.mmocore.manager.MMOManager;

public class EnchantManager extends MMOManager {
	private final Map<Enchantment, Double> base = new HashMap<>();

	public void registerBaseExperience(Enchantment enchant, double value) {
		base.put(enchant, value);
	}

	public double getBaseExperience(Enchantment enchant) {
		return base.get(enchant);
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
