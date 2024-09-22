package net.Indyuce.mmocore.experience.droptable;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.experience.ExperienceObject;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

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
                throw new RuntimeException("Could not load item '" + str + "': " + exception.getMessage());
            }
    }

    public String getId() {
        return id;
    }

    public List<ExperienceItem> getItems() {
        return items;
    }

    /**
     * Called when a player levels up something.
     *
     * @param levelingUp      Player leveling up
     * @param professionLevel New profession level
     * @param object          The object being level up. This MUST be the parent object
     *                        owning the calling experience table! In other words,
     *                        <code>object.getExperienceTable() == this</code> must remain true.
     */
    public void claim(@NotNull PlayerData levelingUp, int professionLevel, @NotNull ExperienceObject object) {

        for (ExperienceItem item : items) {
            final int timesClaimed = levelingUp.getClaims(object, item);
            if (!item.roll(professionLevel, timesClaimed)) continue;

            levelingUp.setClaims(object, item, timesClaimed + 1);
            item.applyTriggers(levelingUp);
        }
    }

    public void unclaim(@NotNull PlayerData playerData, @NotNull ExperienceObject object, boolean reset) {
        for (ExperienceItem item : items) {

            // Undo triggers
            for (int i = 0; i < playerData.getClaims(object, item); i++)
                item.removeTriggers(playerData);

            // Reset levels
            if (reset) playerData.setClaims(object, item, 0);
        }
    }

    /**
     * Called when a player joins. All non-permanent/temporary triggers
     * must be granted back to the player, including player modifiers.
     *
     * @param data   PlayerData
     * @param object Either profession, skillTreeNode or class leveling up
     */
    public void applyTemporaryTriggers(@NotNull PlayerData data, @NotNull ExperienceObject object) {
        for (ExperienceItem item : items) {
            final int timesClaimed = data.getClaims(object, item);
            for (int i = 0; i < timesClaimed; i++)
                item.applyTemporaryTriggers(data);
        }
    }
}
