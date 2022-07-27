package net.Indyuce.mmocore.api.block;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.api.condition.type.BlockCondition;
import io.lumine.mythic.lib.api.condition.type.MMOCondition;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.quest.trigger.Trigger;
import net.Indyuce.mmocore.loot.droptable.DropTable;
import net.Indyuce.mmocore.loot.LootBuilder;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.logging.Level;

public class BlockInfo {
	private final BlockType block;
	private final DropTable table;
	private final RegenInfo regen;
	private final List<Trigger> triggers = new ArrayList<>();
	private final List<BlockCondition> conditions = new ArrayList<>();
	private final Map<BlockInfoOption, Boolean> options = new HashMap<>();

	public BlockInfo(ConfigurationSection config) {
		Validate.notNull(config, "Could not load config");
		Validate.isTrue(config.contains("material"), "Could not find block type");

		block = MMOCore.plugin.loadManager.loadBlockType(new MMOLineConfig(config.getString("material")));
		table = config.contains("drop-table") ? MMOCore.plugin.dropTableManager.loadDropTable(config.get("drop-table")) : null;

		regen = config.contains("regen") ? new RegenInfo(config.getConfigurationSection("regen")) : null;

		if (config.contains("options"))
			for (String key : config.getConfigurationSection("options").getKeys(false))
				try {
					BlockInfoOption option = BlockInfoOption.valueOf(key.toUpperCase().replace("-", "_").replace(" ", "_"));
					options.put(option, config.getBoolean("options." + key));
				} catch (IllegalArgumentException exception) {
					MMOCore.plugin.getLogger().log(Level.WARNING,
							"Could not load option '" + key + "' from block info '" + block.generateKey() + "': " + exception.getMessage());
				}

		if (config.contains("triggers")) {
			List<String> list = config.getStringList("triggers");
			Validate.notNull(list, "Could not load triggers");

			for (String key : list)
				try {
					triggers.addAll(MMOCore.plugin.loadManager.loadTrigger(new MMOLineConfig(key)));
				} catch (IllegalArgumentException exception) {
					MMOCore.plugin.getLogger().log(Level.WARNING,
							"Could not load trigger '" + key + "' from block info '" + block.generateKey() + "': " + exception.getMessage());
				}
		}

		if (config.isList("conditions"))
			for (String key : config.getStringList("conditions")) {
				MMOCondition condition = UtilityMethods.getCondition(key);
				if (condition instanceof BlockCondition)
					conditions.add((BlockCondition) condition);
			}

	}

	public boolean getOption(BlockInfoOption option) {
		return options.getOrDefault(option, option.getDefault());
	}

	public BlockType getBlock() {
		return block;
	}

	@NotNull
	public DropTable getDropTable() {
		return Objects.requireNonNull(table, "Block has no drop table");
	}

	public boolean hasDropTable() {
		return table != null;
	}

	public List<ItemStack> collectDrops(LootBuilder builder) {
		return table != null ? table.collect(builder) : new ArrayList<>();
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

	public boolean hasTriggers() {
		return !triggers.isEmpty();
	}

	public List<Trigger> getTriggers() {
		return triggers;
	}

	public boolean checkConditions(Block block) {
		for (BlockCondition condition : conditions)
			if (!condition.check(block))
				return false;
		return true;
	}

	public static enum BlockInfoOption {

		/**
		 * When disabled, removes the vanilla drops when a block is mined
		 */
		VANILLA_DROPS(true),

		/**
		 * When disabled, removes exp holograms when mined
		 */
		EXP_HOLOGRAMS(true);

		private final boolean def;

		private BlockInfoOption(boolean def) {
			this.def = def;
		}

		public boolean getDefault() {
			return def;
		}
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
