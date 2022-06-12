package net.Indyuce.mmocore.loot.droptable;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import net.Indyuce.mmocore.loot.chest.condition.Condition;
import net.Indyuce.mmocore.loot.chest.condition.ConditionInstance;
import net.Indyuce.mmocore.loot.droptable.dropitem.DropItem;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.loot.LootBuilder;
import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.api.util.PostLoadObject;

public class DropTable extends PostLoadObject {
	private final String id;
	private final Set<DropItem> drops = new LinkedHashSet<>();
	private final Set<Condition> conditions = new LinkedHashSet<>();

	public DropTable(ConfigurationSection config) {
		super(config);

		id = config.getName();
	}

	public DropTable(String id) {
		super(null);

		this.id = id;
	}

	/*
	 * must be loaded after since drop tables must be initialized first
	 * otherwise no reference for drop table drop items.
	 */
	@Override
	protected void whenPostLoaded(ConfigurationSection config) {
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
	}

	public String getId() {
		return id;
	}

	public void registerDropItem(DropItem item) {
		Validate.notNull(item);

		drops.add(item);
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