package net.Indyuce.mmocore.api.block;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

import net.mmogroup.mmolib.UtilityMethods;
import net.mmogroup.mmolib.api.condition.BlockCondition;
import net.mmogroup.mmolib.api.condition.MMOCondition;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.droptable.DropTable;
import net.Indyuce.mmocore.api.loot.LootBuilder;
import net.Indyuce.mmocore.api.quest.trigger.ExperienceTrigger;
import net.Indyuce.mmocore.api.quest.trigger.Trigger;
import net.mmogroup.mmolib.api.MMOLineConfig;

public class BlockInfo {
	private final BlockType block;
	private final DropTable table;
	private final boolean vanillaDrops;
	private final List<Trigger> triggers = new ArrayList<>();
	private final RegenInfo regen;
	private final List<BlockCondition> conditions = new ArrayList<>();

	/*
	 * saved separately because MMOCore needs to display the experience gained,
	 * since it requires a steam call it is better to cache right off the start
	 */
	private final ExperienceTrigger experience;

	public BlockInfo(ConfigurationSection config) {
		Validate.notNull(config, "Could not load config");
		Validate.isTrue(config.contains("material"), "Could not find block type");

		block = MMOCore.plugin.loadManager.loadBlockType(new MMOLineConfig(config.getString("material")));
		table = config.contains("drop-table") ? MMOCore.plugin.dropTableManager.loadDropTable(config.get("drop-table")) : null;
		vanillaDrops = config.getBoolean("vanilla-drops", true);

		regen = config.contains("regen") ? new RegenInfo(config.getConfigurationSection("regen")) : null;

		if (config.contains("triggers")) {
			List<String> list = config.getStringList("triggers");
			Validate.notNull(list, "Could not load triggers");

			for (String key : list)
				try {
					triggers.add(MMOCore.plugin.loadManager.loadTrigger(new MMOLineConfig(key)));
				} catch (IllegalArgumentException exception) {
					MMOCore.plugin.getLogger().log(Level.WARNING,
							"Could not load trigger '" + key + "' from block info '" + block.generateKey() + "': " + exception.getMessage());
				}
		}

		Optional<Trigger> opt = triggers.stream().filter(trigger -> (trigger instanceof ExperienceTrigger)).findFirst();
		experience = (ExperienceTrigger) opt.orElse(null);

		if(config.isList("conditions"))
			for(String key : config.getStringList("conditions")) {
				MMOCondition condition = UtilityMethods.getCondition(key);
				if(condition instanceof BlockCondition) conditions.add((BlockCondition) condition);
			}

	}

	public boolean hasVanillaDrops() {
		return vanillaDrops;
	}

	public BlockType getBlock() {
		return block;
	}

	public DropTable getDropTable() {
		return table;
	}

	public List<ItemStack> collectDrops(LootBuilder builder) {
		return hasDropTable() ? table.collect(builder) : new ArrayList<>();
	}

	public boolean hasDropTable() {
		return table != null;
	}

	public boolean hasRegen() {
		return regen != null;
	}

	public boolean regenerates() {
		return regen != null;
	}

	public RegenInfo getRegenerationInfo() {
		return regen;
	}

	public RegeneratingBlock startRegeneration(BlockData data, Location loc) {
		return new RegeneratingBlock(data, loc, this);
	}

	public boolean hasExperience() {
		return experience != null;
	}

	public ExperienceTrigger getExperience() {
		return experience;
	}

	public boolean hasTriggers() {
		return !triggers.isEmpty();
	}

	public List<Trigger> getTriggers() {
		return triggers;
	}

	public boolean checkConditions(Block block) {
		for(BlockCondition condition : conditions)
			if(!condition.check(block)) return false;

		return true;
	}

	public static class RegeneratingBlock {
		private final BlockData data;
		private final Location loc;
		private final BlockInfo regenerating;

		private final long date = System.currentTimeMillis();

		public RegeneratingBlock(BlockData data, Location loc, BlockInfo regenerating) {
			this.data = data;
			this.loc = loc;
			this.regenerating = regenerating;
		}

		public boolean isTimedOut() {
			return date + regenerating.getRegenerationInfo().getTime() * 50 < System.currentTimeMillis();
		}

		public BlockData getBlockData() {
			return data;
		}

		public Location getLocation() {
			return loc;
		}

		public BlockInfo getRegeneratingBlock() {
			return regenerating;
		}
	}
}
