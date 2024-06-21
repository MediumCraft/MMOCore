package net.Indyuce.mmocore.manager;

import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.gson.JsonParseException;
import net.Indyuce.mmocore.experience.source.type.ExperienceSource;
import net.Indyuce.mmocore.api.block.BlockType;
import net.Indyuce.mmocore.api.load.DefaultMMOLoader;
import net.Indyuce.mmocore.api.load.MMOLoader;
import net.Indyuce.mmocore.api.quest.objective.Objective;
import net.Indyuce.mmocore.api.quest.trigger.Trigger;
import net.Indyuce.mmocore.experience.dispenser.ExperienceDispenser;
import net.Indyuce.mmocore.loot.chest.condition.Condition;
import net.Indyuce.mmocore.loot.droptable.dropitem.DropItem;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class MMOLoadManager {
    private final List<MMOLoader> loaders = new ArrayList<>();

    public MMOLoadManager() {
        loaders.add(new DefaultMMOLoader());
    }

    public void registerLoader(MMOLoader loader) {
        Validate.notNull(loader, "Loader must not be null");

        loaders.add(loader);
    }

    public Condition loadCondition(MMOLineConfig config) {
        return load("condition", config, loader -> loader.loadCondition(config));
    }

    public Objective loadObjective(MMOLineConfig config, ConfigurationSection section) {
        return load("objective", config, loader -> loader.loadObjective(config, section));
    }

    /**
     * Returns a List of Experience Source as one experience source can be linked to others.
     * Loading one exp source can in fact oad multiples if they are linked
     */
    @Deprecated
    public ExperienceSource<?> loadExperienceSource(MMOLineConfig config, ExperienceDispenser dispenser) {
        return load("exp source", config, loader -> loader.loadExperienceSource(config, dispenser));
    }

    public Trigger loadTrigger(MMOLineConfig config) {
        return load("trigger", config, loader -> loader.loadTrigger(config));
    }

    public DropItem loadDropItem(MMOLineConfig config) {
        return load("drop item", config, loader -> loader.loadDropItem(config));
    }

    public BlockType loadBlockType(MMOLineConfig config) {
        return load("block type", config, loader -> loader.loadBlockType(config));
    }

    private <T> T load(String objName, MMOLineConfig config, Function<MMOLoader, T> func) {

        for (MMOLoader loader : loaders)
            try {
                T found = func.apply(loader);
                if (found != null)
                    return found;
            } catch (IllegalArgumentException | JsonParseException | IndexOutOfBoundsException exception) {
                throw new IllegalArgumentException(exception.getMessage());
            }

        throw new IllegalArgumentException("Could not match any " + objName + " to '" + config.getKey() + "' in database");
    }
}
