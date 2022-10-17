package net.Indyuce.mmocore.experience.droptable;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.experience.ExperienceObject;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class ExperienceTable {
    private final String id;
    private final List<ExperienceItem> items = new ArrayList<>();

    public ExperienceTable(ConfigurationSection config) {
        Validate.notNull(config, "Config cannot be null");
        id = config.getName();

        for (String str : config.getKeys(false))
            try {
                Validate.isTrue(config.isConfigurationSection(str), "Key '" + str + "' is not a configuration section");
                items.add(new ExperienceItem(config.getConfigurationSection(str)));
            } catch (RuntimeException exception) {
                MMOCore.plugin.getLogger().log(Level.WARNING, "Could not load item '" + str + "' from experience table '" + id + "': " + exception.getMessage());
            }
    }

    public String getId() {
        return id;
    }

    public List<ExperienceItem> getItems() {
        return items;
    }

    /**
     * Called when a player levels up one of his professions
     *
     * @param levelingUp      Player leveling up
     * @param professionLevel New profession level
     * @param object          Either profession or class leveling up
     */
    public void claim(PlayerData levelingUp, int professionLevel, ExperienceObject object) {

        for (ExperienceItem item : items) {
            int timesClaimed = levelingUp.getClaims(object, this, item);
            if (!item.roll(professionLevel, timesClaimed))
                continue;
            levelingUp.setClaims(object, this, item, timesClaimed + 1);
            item.applyTriggers(levelingUp);
        }
    }

    /**
     * Called when the progression is reset(e.g skill tree reallocation)
     */
    public void reset(PlayerData playerData, ExperienceObject object) {
        for (ExperienceItem item : items) {
            int timesClaimed = playerData.getClaims(object, this, item);
            playerData.setClaims(object, this, item, 0);
            for (int i = 0; i < timesClaimed; i++)
                item.removeStatTriggers(playerData);
        }
    }


    public void removeStatTriggers(PlayerData playerData,ExperienceObject object) {
        for (ExperienceItem item : items) {
            int timesClaimed = playerData.getClaims(object, this, item);
            for (int i = 0; i < timesClaimed; i++)
                item.removeStatTriggers(playerData);
        }
    }


    /**
     * Called when a player joins and all the statTriggers are all triggered back
     *
     * @param data   PlayerData
     * @param object Either profession, skillTreeNode or class leveling up
     */
    public void claimStatTriggers(PlayerData data, ExperienceObject object) {
        for (ExperienceItem item : items) {
            int timesClaimed = data.getClaims(object, this, item);
            for (int i = 0; i < timesClaimed; i++)
                item.applyStatTriggers(data);

        }
    }
}
