package net.Indyuce.mmocore.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.droptable.DropTable;
import net.Indyuce.mmocore.api.droptable.condition.Condition;
import net.Indyuce.mmocore.api.droptable.condition.ConditionInstance;
import net.Indyuce.mmocore.api.load.MMOLineConfig;
import net.Indyuce.mmocore.api.load.MMOLoadException;
import net.Indyuce.mmocore.api.quest.trigger.ExperienceTrigger;
import net.Indyuce.mmocore.api.quest.trigger.Trigger;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.CustomBlock;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.version.VersionMaterial;

public class CustomBlockManager extends MMOManager {
	private final Map<String, BlockInfo> map = new HashMap<>();
	private final Set<RegenInfo> active = new HashSet<>();

	/* list in which both block regen and block permissions are enabled. */
	private final List<Condition> customMineConditions = new ArrayList<>();

	public void loadDropTables(ConfigurationSection config) {
		for (String key : config.getKeys(false))
			try {
				BlockInfo info = new BlockInfo(config.getConfigurationSection(key));
				register(info.getHeadValue().isEmpty() ? info.getCustomBlockID() > 0 ? "mi-custom-" + info.getCustomBlockID() : info.getBlock().name() : info.getHeadValue(), info);
			} catch (IllegalArgumentException exception) {
				MMOCore.log(Level.WARNING, "Could not load custom block '" + key + "': " + exception.getMessage());
			}
	}

	public void register(String key, BlockInfo regen) {
		map.put(key, regen);
	}

	public BlockInfo getInfo(Block block) {
		if(isPlayerSkull(block.getType())) {
			String skullValue = MMOLib.plugin.getNMS().getSkullValue(block);
			return map.getOrDefault(skullValue, map.getOrDefault(block.getType().name(), null));
		}
		if(MMOCore.plugin.isMILoaded())
			if(MMOItems.plugin.getCustomBlocks().isMushroomBlock(block.getType())) {
				CustomBlock cblock = CustomBlock.getFromData(block.getBlockData());
				if(cblock != null)
					return map.getOrDefault("mi-custom-" + cblock.getId(), map.getOrDefault(block.getType().name(), null));
			}
		
		return map.getOrDefault(block.getType().name(), null);
	}

	/*
	 * called when the server disables so every mined block which was in timer
	 * are reset and put back in place.
	 */
	public void resetRemainingBlocks() {
		active.forEach(info -> { regen(info); });
	}

	public void initialize(RegenInfo info) {
		active.add(info);

		if(MMOCore.plugin.isMILoaded() && info.getRegen().getCustomRegenBlockID() != 0) {
			CustomBlock block = MMOItems.plugin.getCustomBlocks().getBlock(info.getRegen().getCustomRegenBlockID());
			info.getLocation().getBlock().setType(block.getType());
			info.getLocation().getBlock().setBlockData(block.getBlockData());
		}
		else info.getLocation().getBlock().setType(info.getRegen().getTemporaryBlock());
		if(isPlayerSkull(info.getLocation().getBlock().getType())) {
			if(isPlayerSkull(info.getRegen().getBlock())) info.getLocation().getBlock().setBlockData(info.getBlockData());
			MMOLib.plugin.getNMS().setSkullValue(info.getLocation().getBlock(), info.getRegen().getRegenHeadValue());
		}

		new BukkitRunnable() {
			public void run() { regen(info); }
		}.runTaskLater(MMOCore.plugin, info.getRegen().getRegenTime());
	}
	
	private void regen(RegenInfo info) {
		info.getLocation().getBlock().setBlockData(info.getBlockData());
		if(isPlayerSkull(info.getLocation().getBlock().getType()))
			MMOLib.plugin.getNMS().setSkullValue(info.getLocation().getBlock(), info.getRegen().getHeadValue());
		active.remove(info);
		
		info.getLocation().getBlock().getState().update();
	}

	public boolean isEnabled(Entity entity) {
		return isEnabled(entity, entity.getLocation());
	}

	public boolean isEnabled(Entity entity, Location loc) {
		ConditionInstance conditionEntity = new ConditionInstance(entity, loc);
		for (Condition condition : customMineConditions)
			if (!condition.isMet(conditionEntity))
				return false;

		return true;
	}
	
	private boolean isPlayerSkull(Material block) {
		return block == VersionMaterial.PLAYER_HEAD.toMaterial() ||
				block == VersionMaterial.PLAYER_WALL_HEAD.toMaterial();
	}

	public class BlockInfo {
		private Material block;
		private final DropTable table;
		private final boolean vanillaDrops;
		private final String headValue;

		private final List<Trigger> triggers = new ArrayList<>();
		private final ExperienceTrigger experience;
		private final int customBlockId;

		/*
		 * options for block regen.
		 */
		private Material temporary;
		private int regenTime = -1;
		private int regenCustomBlockId;
		private String regenHeadValue;

		public BlockInfo(ConfigurationSection config) {
			Validate.notNull(config, "Could not load config");
			block = Material.valueOf(config.getString("material", "BOOKSHELF").toUpperCase().replace("-", "_").replace(" ", "_"));
			customBlockId = config.getInt("mi-custom-block", 0);
			headValue = config.getString("head-value", "");
			table = config.contains("drop-table") ? MMOCore.plugin.dropTableManager.loadDropTable(config.get("drop-table")) : null;
			vanillaDrops = config.getBoolean("vanilla-drops", true);

			if (config.contains("regen")) {
				temporary = Material.valueOf(config.getString("regen.temp-block", "BOOKSHELF").toUpperCase().replace("-", "_").replace(" ", "_"));
				regenCustomBlockId = config.getInt("regen.mi-custom-block", 0);
				regenHeadValue = config.getString("regen.head-value", "");
				
				regenTime = config.getInt("regen.time");
			}

			if (config.contains("triggers")) {
				List<String> list = config.getStringList("triggers");
				Validate.notNull(list, "Could not load triggers");

				for (String key : list)
					try {
						triggers.add(MMOCore.plugin.loadManager.loadTrigger(new MMOLineConfig(key)));
					} catch (MMOLoadException exception) {
						exception.printConsole("BlockRegen", "trigger");
					}
			}

			Optional<Trigger> opt = triggers.stream().filter(trigger -> (trigger instanceof ExperienceTrigger)).findFirst();
			experience = opt.isPresent() ? (ExperienceTrigger) opt.get() : null;
		}

		public String getHeadValue() {
			return headValue;
		}
		
		public boolean hasVanillaDrops() {
			return vanillaDrops;
		}

		public Material getBlock() {
			return block;
		}

		public DropTable getDropTable() {
			return table;
		}

		public List<ItemStack> collectDrops() {
			return hasDropTable() ? table.collect() : new ArrayList<>();
		}

		public boolean hasDropTable() {
			return table != null;
		}

		public boolean hasRegen() {
			return regenTime > 0;
		}

		public int getRegenTime() {
			return regenTime;
		}

		public Material getTemporaryBlock() {
			return temporary;
		}
		
		public String getRegenHeadValue() {
			return regenHeadValue;
		}

		public RegenInfo generateRegenInfo(BlockData data, Location loc) {
			return new RegenInfo(data, loc, this);
		}
		
		public int getCustomBlockID() {
			return customBlockId;
		}
		
		public int getCustomRegenBlockID() {
			return regenCustomBlockId;
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
	}

	public class RegenInfo {
		private final BlockData data;
		private final Location loc;
		private final BlockInfo regen;

		private final long date = System.currentTimeMillis();

		public RegenInfo(BlockData data, Location loc, BlockInfo regen) {
			this.data = data;
			this.loc = loc;
			this.regen = regen;
		}

		public boolean isTimedOut() {
			return date + regen.getRegenTime() * 50 < System.currentTimeMillis();
		}
		
		public BlockData getBlockData() {
			return data;
		}
		
		public Location getLocation() {
			return loc;
		}

		public BlockInfo getRegen() {
			return regen;
		}
	}

	@Override
	public void reload() {
		customMineConditions.clear();

		for (String key : MMOCore.plugin.getConfig().getStringList("custom-mine-conditions"))
			try {
				customMineConditions.add(MMOCore.plugin.loadManager.loadCondition(new MMOLineConfig(key)));
			} catch (MMOLoadException exception) {
				exception.printConsole("CustomMine", "condition");
			}
	}

	@Override
	public void clear() {
		map.clear();
	}
}
