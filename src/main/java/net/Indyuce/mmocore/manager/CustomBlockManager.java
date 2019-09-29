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
import net.Indyuce.mmocore.version.VersionMaterial;

public class CustomBlockManager extends MMOManager {
	private final Map<Material, BlockInfo> map = new HashMap<>();
	private final Map<String, BlockInfo> headmap = new HashMap<>();
	private final Set<RegenInfo> active = new HashSet<>();

	/*
	 * list in which both block regen and block permissions are enabled.
	 */
	private final List<Condition> customMineConditions = new ArrayList<>();

	public void loadDropTables(ConfigurationSection config) {
		for (String key : config.getKeys(false))
			try {
				register(new BlockInfo(config.getConfigurationSection(key), false));
			} catch (IllegalArgumentException exception) {
				MMOCore.log(Level.WARNING, "Could not load custom block '" + key + "': " + exception.getMessage());
			}
	}

	public void loadPHDropTables(ConfigurationSection config) {
		for (String key : config.getKeys(false))
			try {
				register(new BlockInfo(config.getConfigurationSection(key), true));
			} catch (IllegalArgumentException exception) {
				MMOCore.log(Level.WARNING, "Could not load custom block '" + key + "': " + exception.getMessage());
			}
	}

	public void register(BlockInfo regen) {
		if(!regen.headValue.isEmpty()) {
			MMOCore.log("Reistered Head Value: " + regen.headValue);
			headmap.put(regen.headValue, regen);
		}
		else
			map.put(regen.getBlock(), regen);
	}

	public BlockInfo getInfo(Block block) {
		if(block.getType() == VersionMaterial.PLAYER_HEAD.toMaterial()) {
			String skullValue = MMOCore.plugin.nms.getSkullValue(block);
			
			return headmap.containsKey(skullValue) ? headmap.get(skullValue) : null;
		}
			
		return map.containsKey(block.getType()) ? map.get(block.getType()) : null;
	}

	/*
	 * called when the server disables so every mined block which was in timer
	 * are reset and put back in place.
	 */
	public void resetRemainingBlocks() {
		active.forEach(info -> info.getLocation().getBlock().setType(info.getRegen().getBlock()));
	}

	public void initialize(RegenInfo info) {

		active.add(info);
		info.getLocation().getBlock().setType(info.getRegen().getTemporaryBlock());
		if(info.getRegen().getTemporaryBlock() == Material.PLAYER_HEAD)
			MMOCore.plugin.nms.setSkullValue(info.getLocation().getBlock(), info.getRegen().regenHeadValue);

		new BukkitRunnable() {
			public void run() {
				active.remove(info);
				info.getLocation().getBlock().setType(info.getRegen().getBlock());
				if(info.getRegen().getBlock() == Material.PLAYER_HEAD) {
					MMOCore.plugin.nms.setSkullValue(info.getLocation().getBlock(), info.getRegen().headValue);
					info.getLocation().getBlock().getState().update();
				}
			}
		}.runTaskLater(MMOCore.plugin, info.getRegen().getRegenTime());
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

	public class BlockInfo {
		private final Material block;
		private final DropTable table;
		private final boolean vanillaDrops;
		private final String headValue;

		private final List<Trigger> triggers = new ArrayList<>();
		private final ExperienceTrigger experience;

		/*
		 * options for block regen.
		 */
		private Material temporary;
		private int regenTime = -1;
		private String regenHeadValue;

		public BlockInfo(ConfigurationSection config, boolean isPlayerHead) {
			Validate.notNull(config, "Could not load config");
			block = isPlayerHead ? Material.PLAYER_HEAD : Material.valueOf(config.getName().toUpperCase().replace("-", "_").replace(" ", "_"));
			headValue = isPlayerHead ? config.getString("head-value") : "";
			table = config.contains("drop-table") ? MMOCore.plugin.dropTableManager.loadDropTable(config.get("drop-table")) : null;
			vanillaDrops = config.getBoolean("vanilla-drops", true);

			if (config.contains("regen")) {
				String format = config.getString("regen.temp-block");
				Validate.notNull(config, "Could not load temporary block");
				temporary = isPlayerHead ? Material.PLAYER_HEAD : Material.valueOf(format.toUpperCase().replace("-", "_").replace(" ", "_"));
				if(temporary == Material.PLAYER_HEAD)
					regenHeadValue = config.getString("regen.head-value");
				
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

		public RegenInfo generateRegenInfo(Location loc) {
			return new RegenInfo(loc, this);
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
		private final Location loc;
		private final BlockInfo regen;

		private final long date = System.currentTimeMillis();

		public RegenInfo(Location loc, BlockInfo regen) {
			this.loc = loc;
			this.regen = regen;
		}

		public boolean isTimedOut() {
			return date + regen.getRegenTime() * 50 < System.currentTimeMillis();
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
		headmap.clear();
	}
}
