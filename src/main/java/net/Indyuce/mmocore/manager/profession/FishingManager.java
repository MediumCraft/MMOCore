package net.Indyuce.mmocore.manager.profession;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.droptable.condition.Condition;
import net.Indyuce.mmocore.api.droptable.condition.ConditionInstance;
import net.Indyuce.mmocore.api.droptable.dropitem.fishing.FishingDropItem;
import net.Indyuce.mmocore.manager.MMOManager;
import net.mmogroup.mmolib.api.MMOLineConfig;

public class FishingManager extends MMOManager {
	private final Set<FishingDropTable> tables = new LinkedHashSet<>();

	private static final Random random = new Random();

	public void loadDropTables(ConfigurationSection config) {
		for (String key : config.getKeys(false))
			try {
				tables.add(new FishingDropTable(config.getConfigurationSection(key)));
			} catch (IllegalArgumentException exception) {
				MMOCore.log(Level.WARNING, "Could not load fishing drop table " + key + ": " + exception.getMessage());
			}
	}

	public FishingDropTable calculateDropTable(Entity entity) {
		ConditionInstance conditionEntity = new ConditionInstance(entity);

		for (FishingDropTable table : tables)
			if (table.areConditionsMet(conditionEntity))
				return table;

		return null;
	}

	public class FishingDropTable {
		private final String id;

		private final Set<Condition> conditions = new HashSet<>();
		private final List<FishingDropItem> items = new ArrayList<>();
		private int maxWeight = 0;

		public FishingDropTable(ConfigurationSection section) {
			Validate.notNull(section, "Could not load config");
			this.id = section.getName();

			if (section.contains("conditions")) {
				List<String> list = section.getStringList("conditions");
				Validate.notNull(list, "Could not load conditions");

				for (String str : list)
					try {
						conditions.add(MMOCore.plugin.loadManager.loadCondition(new MMOLineConfig(str)));
					} catch (IllegalArgumentException exception) {
						MMOCore.plugin.getLogger().log(Level.WARNING,
								"Could not load condition '" + str + "' from fishing drop table '" + id + "': " + exception.getMessage());
					}
			}

			List<String> list = section.getStringList("items");
			Validate.notNull(list, "Could not load item list");

			for (String str : list)
				try {
					FishingDropItem dropItem = new FishingDropItem(str);
					maxWeight += dropItem.getWeight();
					items.add(dropItem);
				} catch (IllegalArgumentException | IndexOutOfBoundsException exception) {
					MMOCore.plugin.getLogger().log(Level.WARNING,
							"Could not load item '" + str + "' from fishing drop table '" + id + "': " + exception.getMessage());
				}

			Validate.notEmpty(items, "The item list must not be empty.");
		}

		public boolean areConditionsMet(ConditionInstance entity) {
			for (Condition condition : conditions)
				if (!condition.isMet(entity))
					return false;
			return true;
		}

		public Set<Condition> getConditions() {
			return conditions;
		}

		public FishingDropItem getRandomItem() {
			int weight = random.nextInt(maxWeight);

			for (FishingDropItem item : items) {
				weight -= item.getWeight();

				if (weight <= 0)
					return item;
			}

			throw new NullPointerException("Could not find item in drop table");
		}
	}

	@Override
	public void reload() {
		// TODO Auto-generated method stub

	}

	@Override
	public void clear() {
		tables.clear();
	}
}
