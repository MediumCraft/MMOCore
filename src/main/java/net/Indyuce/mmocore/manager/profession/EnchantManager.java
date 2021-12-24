package net.Indyuce.mmocore.manager.profession;

import io.lumine.mythic.lib.MythicLib;
import net.Indyuce.mmocore.MMOCore;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class EnchantManager extends SpecificProfessionManager {
	private final Map<Enchantment, Double> base = new HashMap<>();

	public EnchantManager() {
		super("base-enchant-exp");
	}

    @Override
    public void loadProfessionConfiguration(ConfigurationSection config) {
        for (String key : config.getKeys(false))
            try {
                Enchantment enchant = MythicLib.plugin.getVersion().getWrapper().getEnchantmentFromString(key.toLowerCase().replace("-", "_"));
                MMOCore.plugin.enchantManager.registerBaseExperience(enchant, config.getDouble(key));
            } catch (IllegalArgumentException exception) {
                MMOCore.log(Level.WARNING, "Could not find enchant with name '" + key + "'");
            }
    }

	public void registerBaseExperience(Enchantment enchant, double value) {
		base.put(enchant, value);
	}

	public double getBaseExperience(Enchantment enchant) {
		return base.get(enchant);
	}

	@Override
	public void initialize(boolean clearBefore) {
		if (clearBefore)
			base.clear();
	}
}
