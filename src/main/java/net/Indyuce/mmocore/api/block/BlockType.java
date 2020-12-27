package net.Indyuce.mmocore.api.block;

import org.bukkit.block.Block;

import net.Indyuce.mmocore.api.block.BlockInfo.RegeneratingBlock;

public interface BlockType {

	/**
	 * Called when placing temporary blocks
	 */
	void place(RegeneratingBlock placed);

	/**
	 * Called when regenerating an older block with block regen
	 */
	void regenerate(RegeneratingBlock regenerating);

	/**
	 * Generates a key used to store the BlockInfo instance in the manager map,
	 * the key depends on the block type to make sure there is no interference
	 */
	String generateKey();

	/**
	 * Applies some extra break restrictions; returns TRUE if the block can be
	 * broken. This method is used to prevent non mature crops from being broken
	 * for example
	 */
	boolean breakRestrictions(Block block);
}
