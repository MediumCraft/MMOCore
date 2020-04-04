package net.Indyuce.mmocore.api.block;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import net.Indyuce.mmocore.api.block.BlockInfo.RegeneratingBlock;
import net.Indyuce.mmoitems.api.util.MMOLineConfig;

public class VanillaBlockType implements BlockType {
	private final Material type;

	public VanillaBlockType(MMOLineConfig config) {
		config.validate("type");

		type = Material.valueOf(config.getString("type").toUpperCase().replace("-", "_").replace(" ", "_"));
	}

	public VanillaBlockType(Block block) {
		type = block.getType();
	}

	public Material getType() {
		return type;
	}

	@Override
	public void place(Location loc, RegeneratingBlock block) {
		loc.getBlock().setType(type);
	}

	@Override
	public String generateKey() {
		return "vanilla-block-" + type.name();
	}
}
