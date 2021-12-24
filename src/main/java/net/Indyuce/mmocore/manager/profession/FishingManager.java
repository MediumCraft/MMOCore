package net.Indyuce.mmocore.manager.profession;

import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.loot.droptable.condition.Condition;
import net.Indyuce.mmocore.loot.droptable.condition.ConditionInstance;
import net.Indyuce.mmocore.loot.droptable.dropitem.fishing.FishingDropItem;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import java.util.*;
import java.util.logging.Level;

public class FishingManager extends SpecificProfessionManager {
	private final Set<FishingDropTable> tables = new LinkedHashSet<>();

	private static final Random random = new Random();

	public FishingManager() {
		super("on-fish");
	}

	@Override
	public void loadProfessionConfiguration(ConfigurationSection config) {
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

	public static class FishingDropTable {
		private final Set<Condition> conditions = new HashSet<>();
		private final List<FishingDropItem> items = new ArrayList<>();
		private int maxWeight = 0;

		public FishingDropTable(ConfigurationSection section) {
			Validate.notNull(section, "Could not load config");
			final String id = section.getName();

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
	public void initialize(boolean clearBefore) {
		if (clearBefore)
			tables.clear();
	}
}
