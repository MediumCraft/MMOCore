package net.Indyuce.mmocore.manager.profession;

import net.Indyuce.mmocore.manager.MMOCoreManager;
import org.bukkit.configuration.ConfigurationSection;

public abstract class SpecificProfessionManager implements MMOCoreManager {

    /**
     * String key used to detect and load profession config in any
     * profession.yml config
     */
    private final String key;

    public SpecificProfessionManager(String key) {
        this.key = key;
    }

    public String getStringKey() {
        return key;
    }

    public abstract void loadProfessionConfiguration(ConfigurationSection config);
}
