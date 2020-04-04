package net.Indyuce.mmocore.api.block;

import org.bukkit.Location;
import org.bukkit.block.Block;

import net.Indyuce.mmocore.api.block.BlockInfo.RegeneratingBlock;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.MMOLineConfig;

public class SkullBlockType implements BlockType {
	private final String value;

	public SkullBlockType(MMOLineConfig config) {
		config.validate("value");

		value = config.getString("value");
	}

	public SkullBlockType(Block block) {
		value = MMOLib.plugin.getNMS().getSkullValue(block);
	}

	public String getValue() {
		return value;
	}

	@Override
	public void place(Location loc, RegeneratingBlock block) {

		// save skull orientation if replaced block is a player head
		if (MMOCoreUtils.isPlayerHead(loc.getBlock().getType()) && MMOLib.plugin.getVersion().isStrictlyHigher(1, 12))
			loc.getBlock().setBlockData(block.getBlockData());

		MMOLib.plugin.getNMS().setSkullValue(loc.getBlock(), value);
	}

	@Override
	public String generateKey() {
		return "vanilla-skull-" + value;
	}
}
