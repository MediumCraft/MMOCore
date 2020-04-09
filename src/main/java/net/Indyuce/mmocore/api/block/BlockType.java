package net.Indyuce.mmocore.api.block;

import org.bukkit.Location;

import net.Indyuce.mmocore.api.block.BlockInfo.RegeneratingBlock;

public interface BlockType {
	public void place(Location loc, RegeneratingBlock regenerating);

	// public boolean matches(Block block);

	public String generateKey();
}
