package net.Indyuce.mmocore.manager.profession;

import net.Indyuce.mmocore.experience.Profession;
import net.Indyuce.mmocore.manager.MMOCoreManager;
import org.bukkit.configuration.ConfigurationSection;

public abstract class SpecificProfessionManager implements MMOCoreManager {

    /**
     * String key used to detect and load profession config in any
     * profession.yml config
     */
    private final String key;

    /**
     * Caching the profession that is linked to the profession manager.
     */
    private Profession linkedProfession;

    public SpecificProfessionManager(String key) {
        this.key = key;
    }

    public void setLinkedProfession(Profession linkedProfession) {
        this.linkedProfession = linkedProfession;
    }

    public Profession getLinkedProfession() {
        return linkedProfession;
    }

    public boolean hasLinkedProfession() {
        return linkedProfession != null;
    }

    public String getStringKey() {
        return key;
    }

    public abstract void loadProfessionConfiguration(ConfigurationSection config);
}
