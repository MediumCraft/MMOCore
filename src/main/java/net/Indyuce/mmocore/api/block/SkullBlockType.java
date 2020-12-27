package net.Indyuce.mmocore.api.block;

import net.Indyuce.mmocore.api.block.BlockInfo.RegeneratingBlock;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.MMOLineConfig;
import net.mmogroup.mmolib.version.VersionMaterial;
import org.bukkit.Location;
import org.bukkit.block.Block;

public class SkullBlockType implements BlockType {
	private final String value;

	public SkullBlockType(MMOLineConfig config) {
		config.validate("value");

		value = config.getString("value");
	}

	public SkullBlockType(Block block) {
		value = MMOLib.plugin.getVersion().getWrapper().getSkullValue(block);
	}

	public String getValue() {
		return value;
	}

	@Override
	public void place(Location loc, RegeneratingBlock block) {
		loc.getBlock().setType(VersionMaterial.PLAYER_HEAD.toMaterial());

		// save skull orientation if replaced block is a player head
		if (MMOCoreUtils.isPlayerHead(block.getBlockData().getMaterial()))
			loc.getBlock().setBlockData(block.getBlockData());

		MMOLib.plugin.getVersion().getWrapper().setSkullValue(loc.getBlock(), value);
	}

	@Override
	public void regen(Location loc, RegeneratingBlock block) {
		// This makes sure that if a skull loses it's original rotation
		// it can revert back to it when the base block is regenerated
		loc.getBlock().setBlockData(block.getBlockData());
		MMOLib.plugin.getVersion().getWrapper().setSkullValue(loc.getBlock(), value);
	}


	@Override
	public String generateKey() {
		return "vanilla-skull-" + value;
	}

	@Override
	public boolean breakRestrictions(Block block) {
		return true;
	}
}
