package net.Indyuce.mmocore.api.block;

import org.bukkit.Location;
import org.bukkit.block.Block;

import net.Indyuce.mmocore.api.block.BlockInfo.RegeneratingBlock;

public interface BlockType {
	public void place(Location loc, RegeneratingBlock regenerating);

	// public boolean matches(Block block);

	/*
	 * generates a key used to store the BlockInfo instance in the manager map, the
	 * key depends on the block type to make sure there is no interference
	 */
	public String generateKey();

	/*
	 * generateKey() determines if the block is handled by that block type,
	 * breakRestrictions(Block) applies some extra break restrictions; returns TRUE
	 * if the block can be broken
	 */
	public boolean breakRestrictions(Block block);
}
