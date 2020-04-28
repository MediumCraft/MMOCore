package net.Indyuce.mmocore.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.block.BlockInfo;
import net.Indyuce.mmocore.api.block.BlockInfo.RegeneratingBlock;
import net.Indyuce.mmocore.api.block.BlockType;
import net.Indyuce.mmocore.api.block.SkullBlockType;
import net.Indyuce.mmocore.api.block.VanillaBlockType;
import net.Indyuce.mmocore.api.droptable.condition.Condition;
import net.Indyuce.mmocore.api.droptable.condition.ConditionInstance;
import net.Indyuce.mmocore.api.load.MMOLoadException;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import net.mmogroup.mmolib.api.MMOLineConfig;

public class CustomBlockManager extends MMOManager {

	/*
	 * registered block infos
	 */
	private final Map<String, BlockInfo> map = new HashMap<>();

	/*
	 * blocks that are regenerating and that must be refreshed whenever the
	 * server reloads or shuts down not to hurt the world map
	 */
	private final Set<RegeneratingBlock> active = new HashSet<>();

	/* list in which both block regen and block permissions are enabled. */
	private final List<Condition> customMineConditions = new ArrayList<>();

	/*
	 * list of functions which let MMOCore recognize what block a player is
	 * currently breaking
	 */
	private final List<Function<Block, Optional<BlockType>>> blockTypes = new ArrayList<>();

	public CustomBlockManager() {
		registerBlockType(block -> MMOCoreUtils.isPlayerHead(block.getType()) ? Optional.of(new SkullBlockType(block)) : Optional.empty());
	}

	public void registerBlockType(Function<Block, Optional<BlockType>> function) {
		blockTypes.add(function);
	}

	public void register(BlockInfo regen) {
		map.put(regen.getBlock().generateKey(), regen);
	}

	public BlockInfo getInfo(Block block) {
		return map.getOrDefault(findBlockType(block).generateKey(), null);
	}

	public BlockType findBlockType(Block block) {
		for (Function<Block, Optional<BlockType>> blockType : blockTypes) {
			Optional<BlockType> type = blockType.apply(block);
			if (type.isPresent())
				return type.get();
		}

		return new VanillaBlockType(block);
	}

	public void initialize(RegeneratingBlock info) {
		active.add(info);
		if (info.getRegeneratingBlock().getRegenerationInfo().hasTemporaryBlock())
			info.getRegeneratingBlock().getRegenerationInfo().getTemporaryBlock().place(info.getLocation(), info);
		Bukkit.getScheduler().runTaskLater(MMOCore.plugin, () -> regen(info), info.getRegeneratingBlock().getRegenerationInfo().getTime());
	}

	private void regen(RegeneratingBlock info) {
		info.getRegeneratingBlock().getBlock().place(info.getLocation(), info);
		active.remove(info);
		info.getLocation().getBlock().getState().update();
	}

	/*
	 * called when the server disables so every mined block which was in timer
	 * are reset and put back in place.
	 */
	public void resetRemainingBlocks() {
		active.forEach(info -> regen(info));
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

	public void loadDropTables(ConfigurationSection config) {
		for (String key : config.getKeys(false))
			try {
				register(new BlockInfo(config.getConfigurationSection(key)));
			} catch (IllegalArgumentException exception) {
				MMOCore.log(Level.WARNING, "Could not load custom block '" + key + "': " + exception.getMessage());
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
