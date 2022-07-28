package net.Indyuce.mmocore.manager.profession;

import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.loot.RandomWeightedRoll;
import net.Indyuce.mmocore.loot.chest.condition.Condition;
import net.Indyuce.mmocore.loot.chest.condition.ConditionInstance;
import net.Indyuce.mmocore.loot.fishing.FishingDropItem;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import java.util.*;
import java.util.logging.Level;

public class FishingManager extends SpecificProfessionManager {
	private final Set<FishingDropTable> tables = new LinkedHashSet<>();

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

		// Link fishing stats to this profession
		MMOCore.plugin.statManager.registerProfession("FISHING_STRENGTH", getLinkedProfession());
		MMOCore.plugin.statManager.registerProfession("CRITICAL_FISHING_CHANCE", getLinkedProfession());
		MMOCore.plugin.statManager.registerProfession("CRITICAL_FISHING_FAILURE_CHANCE", getLinkedProfession());
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

		public FishingDropTable(ConfigurationSection section) {
			Validate.notNull(section, "Could not load config");
			final String id = section.getName();

			if (section.contains("conditions")) {
				List<String> list = section.getStringList("conditions");
				Validate.notNull(list, "Could not load conditions");

				for (String str : list)
					try {
						conditions.addAll(MMOCore.plugin.loadManager.loadCondition(new MMOLineConfig(str)));
					} catch (IllegalArgumentException exception) {
						MMOCore.plugin.getLogger().log(Level.WARNING,
								"Could not load condition '" + str + "' from fishing drop table '" + id + "': " + exception.getMessage());
					}
			}

			List<String> list = section.getStringList("items");
			Validate.notNull(list, "Could not load item list");

			for (String str : list)
				try {
					FishingDropItem dropItem = new FishingDropItem(new MMOLineConfig(str));
					items.add(dropItem);
				} catch (RuntimeException exception) {
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

		/**
		 * The chance stat will make low weight items more
		 * likely to be chosen by the algorithm.
		 *
		 * @return Randomly computed fishing drop item
		 */
		public FishingDropItem getRandomItem(PlayerData player) {
			return new RandomWeightedRoll<>(player, items, MMOCore.plugin.configManager.fishingDropsChanceWeight).rollItem();
		}
	}

	@Override
	public void initialize(boolean clearBefore) {
		if (clearBefore)
			tables.clear();
	}
}
