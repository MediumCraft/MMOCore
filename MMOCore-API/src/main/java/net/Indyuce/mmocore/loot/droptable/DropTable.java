package net.Indyuce.mmocore.loot.droptable;

import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.util.PostLoadAction;
import io.lumine.mythic.lib.util.PreloadedObject;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.loot.LootBuilder;
import net.Indyuce.mmocore.loot.chest.condition.Condition;
import net.Indyuce.mmocore.loot.chest.condition.ConditionInstance;
import net.Indyuce.mmocore.loot.droptable.dropitem.DropItem;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public class DropTable implements PreloadedObject {
    private final String id;
    private final Set<DropItem> drops = new LinkedHashSet<>();
    private final Set<Condition> conditions = new LinkedHashSet<>();

    private final PostLoadAction postLoadAction;

    public DropTable(ConfigurationSection config) {
        this.postLoadAction = generatePostLoadAction();
        this.postLoadAction.cacheConfig(config);

        this.id = config.getName();
    }

    public DropTable(String id) {
        this.postLoadAction = generatePostLoadAction();
        this.id = id;
    }

    private PostLoadAction generatePostLoadAction() {
        return new PostLoadAction(config -> {
            List<String> itemsList = config.getStringList("items");
            List<String> conditionsList = config.getStringList("conditions");
            Validate.notNull(itemsList, "Could not find drop item list");

            for (String key : itemsList)
                try {
                    drops.add(MMOCore.plugin.loadManager.loadDropItem(new MMOLineConfig(key)));
                } catch (IllegalArgumentException exception) {
                    MMOCore.plugin.getLogger().log(Level.WARNING,
                            "Could not load drop item '" + key + "' from table '" + id + "': " + exception.getMessage());
                }
            for (String key : conditionsList)
                try {
                    conditions.add(MMOCore.plugin.loadManager.loadCondition(new MMOLineConfig(key)));
                } catch (IllegalArgumentException exception) {
                    MMOCore.plugin.getLogger().log(Level.WARNING,
                            "Could not load condition '" + key + "' from table '" + id + "': " + exception.getMessage());
                }
        });
    }

    @NotNull
    @Override
    public PostLoadAction getPostLoadAction() {
        return postLoadAction;
    }

    public String getId() {
        return id;
    }

    public void registerDropItem(DropItem item) {
        Validate.notNull(item);

        drops.add(item);
    }

    public Set<DropItem> getDrops() {
        return drops;
    }

    public List<ItemStack> collect(LootBuilder builder) {

        for (DropItem item : drops)
            if (item.rollChance(builder.getEntity()) && builder.getCapacity() >= item.getWeight()) {
                item.collect(builder);
                builder.reduceCapacity(item.getWeight());
            }

        return builder.getLoot();
    }

    public Set<Condition> getConditions() {
        return conditions;
    }

    public boolean areConditionsMet(ConditionInstance entity) {
        for (Condition condition : conditions)
            if (!condition.isMet(entity))
                return false;
        return true;
    }
}