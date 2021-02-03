package net.Indyuce.mmocore.api.block;

import org.bukkit.Location;
import org.bukkit.block.Block;

import net.Indyuce.mmocore.api.block.BlockInfo.RegeneratingBlock;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.version.VersionMaterial;

public class SkullBlockType implements BlockType {
	private final String value;

	public SkullBlockType(MMOLineConfig config) {
		config.validate("value");

		value = config.getString("value");
	}

	public SkullBlockType(Block block) {
		value = MythicLib.plugin.getVersion().getWrapper().getSkullValue(block);
	}

	public String getValue() {
		return value;
	}

	@Override
	public void place(RegeneratingBlock block) {
		Location loc = block.getLocation();
		loc.getBlock().setType(VersionMaterial.PLAYER_HEAD.toMaterial());

		// save skull orientation if replaced block is a player head
		if (MMOCoreUtils.isPlayerHead(block.getBlockData().getMaterial()))
			loc.getBlock().setBlockData(block.getBlockData());

		MythicLib.plugin.getVersion().getWrapper().setSkullValue(loc.getBlock(), value);
	}

	@Override
	public void regenerate(RegeneratingBlock block) {
		Location loc = block.getLocation();
		// This makes sure that if a skull loses its original rotation
		// it can revert back to it when the base block is regenerated
		loc.getBlock().setBlockData(block.getBlockData());
		MythicLib.plugin.getVersion().getWrapper().setSkullValue(loc.getBlock(), value);
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
