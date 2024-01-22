package net.Indyuce.mmocore.api.block;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;

import net.Indyuce.mmocore.api.block.BlockInfo.RegeneratingBlock;
import io.lumine.mythic.lib.api.MMOLineConfig;

public class VanillaBlockType implements BlockType {
	private final Material type;

	/*
	 * allows to plant back crops with a custom age so that it does not always
	 * have to full grow again-
	 */
	private final int age;

	public VanillaBlockType(MMOLineConfig config) {
		config.validate("type");

		type = Material.valueOf(config.getString("type").toUpperCase().replace("-", "_").replace(" ", "_"));
		age = config.getInt("age", 0);

		Validate.isTrue(age >= 0 && age < 8, "Age must be between 0 and 7");
	}

	public VanillaBlockType(Block block) {
		type = block.getType();
		age = 0;
	}

	public Material getType() {
		return type;
	}

	@Override
	public void place(RegeneratingBlock block) {
		Location loc = block.getLocation();
		block.getLocation().getBlock().setType(type);

		BlockData state = block.getLocation().getBlock().getBlockData();
		if (age > 0 && state instanceof Ageable) {
			((Ageable) state).setAge(age);
			loc.getBlock().setBlockData(state);
		}
	}

	@Override
	public void regenerate(RegeneratingBlock block) {
		Location loc = block.getLocation();
		loc.getBlock().setType(type);
		// Sets the original blocks old data (only when regenerating)
		loc.getBlock().setBlockData(block.getBlockData());
	}

	@Override
	public String generateKey() {
		return "vanilla-block-" + type.name();
	}

	@Override
	public boolean breakRestrictions(Block block) {
		return age == 0 || (block.getBlockData() instanceof Ageable && ((Ageable) block.getBlockData()).getAge() >= age);
	}
}
