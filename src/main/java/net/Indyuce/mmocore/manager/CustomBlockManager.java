package net.Indyuce.mmocore.manager;

import io.papermc.lib.PaperLib;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.block.BlockInfo;
import net.Indyuce.mmocore.api.block.BlockInfo.RegeneratingBlock;
import net.Indyuce.mmocore.api.block.BlockType;
import net.Indyuce.mmocore.api.block.SkullBlockType;
import net.Indyuce.mmocore.api.block.VanillaBlockType;
import net.Indyuce.mmocore.api.droptable.condition.Condition;
import net.Indyuce.mmocore.api.droptable.condition.ConditionInstance;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import net.mmogroup.mmolib.api.MMOLineConfig;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;

public class CustomBlockManager extends MMOManager {

	/**
	 * Registered block infos
	 */
	private final Map<String, BlockInfo> map = new HashMap<>();

	/**
	 * Blocks that are regenerating and that must be refreshed whenever the
	 * server reloads or shuts down not to hurt the world map
	 */
	private final Set<RegeneratingBlock> active = new HashSet<>();

	/**
	 * Stores conditions which must be met to apply custom mining
	 */
	private final List<Condition> customMineConditions = new ArrayList<>();

	/**
	 * List of functions which let MMOCore recognize what block a player is
	 * currently breaking
	 */
	private final List<Function<Block, Optional<BlockType>>> blockTypes = new ArrayList<>();

	private boolean protect;

	public CustomBlockManager() {
		registerBlockType(block -> MMOCoreUtils.isPlayerHead(block.getType()) ? Optional.of(new SkullBlockType(block)) : Optional.empty());
	}

	public void registerBlockType(Function<Block, Optional<BlockType>> function) {
		blockTypes.add(function);
	}

	public void register(BlockInfo regen) {
		map.put(regen.getBlock().generateKey(), regen);
	}

	/**
	 * Checks if the behaviour of a block was changed by a specific profession
	 * (different drop tables, block regen..)
	 * 
	 * @param  block Block to check
	 * @return       The new block behaviour or null if no new behaviour
	 */
	public @Nullable BlockInfo getInfo(Block block) {
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

	/**
	 * Used when a block is being broken and MMOCore needs to regen it after X
	 * seconds. Also places the temporary block at the block location
	 * 
	 * @param info          Block info
	 * @param scheduleRegen If block regeneration should be scheduled or not. If
	 *                      the block broken is a temporary block and is part of
	 *                      a "block chain", no regen should be scheduled as
	 *                      there is already one
	 */
	public void initialize(RegeneratingBlock info, boolean scheduleRegen) {
		if (scheduleRegen) {
			active.add(info);
			Bukkit.getScheduler().runTaskLater(MMOCore.plugin, () -> regen(info, false), info.getRegeneratingBlock().getRegenerationInfo().getTime());
		}

		if (info.getRegeneratingBlock().getRegenerationInfo().hasTemporaryBlock())
			info.getRegeneratingBlock().getRegenerationInfo().getTemporaryBlock().place(info);
	}

	/**
	 * Called when a block regens, either due to regen timer or because the
	 * server shuts down.
	 * 
	 * @param info     Block which must be regened
	 * @param shutdown Must be set to true if the server is shutting down. When
	 *                 the server shuts down, it iterates through active blocks.
	 *                 This prevents any issue when editing lists being iterated
	 */
	private void regen(RegeneratingBlock info, boolean shutdown) {

		// Get the chunk and load it async if needed.
		PaperLib.getChunkAtAsync(info.getLocation()).whenComplete((chunk, ex) -> {
			info.getRegeneratingBlock().getBlock().regenerate(info);
			info.getLocation().getBlock().getState().update();
			if (!shutdown)
				active.remove(info);
		});
	}

	/**
	 * Called when the server disables so every mined block which was in timer
	 * are reset and put back in place.
	 */
	public void resetRemainingBlocks() {
		active.forEach(info -> regen(info, true));
	}

	/**
	 * @param  block Potentially vanilla block being broken by a player
	 * @return       Returns if the block being broken is a temporary block. If
	 *               it is, players should not be able to break it
	 */
	public boolean isTemporaryBlock(Block block) {
		Location loc = block.getLocation();
		for (RegeneratingBlock info : active)
			if (info.getLocation().getBlockX() == loc.getBlockX() && info.getLocation().getBlockY() == loc.getBlockY()
					&& info.getLocation().getBlockZ() == loc.getBlockZ())
				return true;

		return false;
	}

	public boolean isEnabled(Entity entity) {
		return isEnabled(entity, entity.getLocation());
	}

	public boolean isEnabled(Entity entity, Location loc) {
		if (customMineConditions.isEmpty())
			return false;

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

	/**
	 * @return If block breaking should be denied in custom mining regions
	 */
	public boolean shouldProtect() {
		return protect;
	}

	@Override
	public void reload() {
		customMineConditions.clear();

		for (String key : MMOCore.plugin.getConfig().getStringList("custom-mine-conditions"))
			try {
				customMineConditions.add(MMOCore.plugin.loadManager.loadCondition(new MMOLineConfig(key)));
			} catch (IllegalArgumentException exception) {
				MMOCore.plugin.getLogger().log(Level.WARNING, "Could not load custom mining condition '" + key + "': " + exception.getMessage());
			}
	}

	@Override
	public void clear() {
		map.clear();
	}
}
