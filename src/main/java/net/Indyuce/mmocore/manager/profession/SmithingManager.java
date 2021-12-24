package net.Indyuce.mmocore.manager.profession;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;

public class SmithingManager extends SpecificProfessionManager {
    private final Map<Material, Double> base = new HashMap<>();

    public SmithingManager() {
        super("repair-exp");
    }

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
    public void loadProfessionConfiguration(ConfigurationSection config) {
        for (String key : config.getKeys(false))
            try {
                Material material = Material.valueOf(key.toUpperCase().replace("-", "_").replace(" ", "_"));
                registerBaseExperience(material, config.getDouble(key));
            } catch (IllegalArgumentException exception) {
                throw new RuntimeException("Could not read material from '" + key + "'");
            }
    }

    @Override
    public void initialize(boolean clearBefore) {
        if (clearBefore)
            base.clear();
    }
}
