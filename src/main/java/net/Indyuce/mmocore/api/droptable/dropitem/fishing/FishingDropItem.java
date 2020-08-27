package net.Indyuce.mmocore.api.droptable.dropitem.fishing;

import org.apache.commons.lang.Validate;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.droptable.dropitem.DropItem;
import net.Indyuce.mmocore.api.loot.LootBuilder;
import net.Indyuce.mmocore.api.util.math.formula.RandomAmount;
import net.mmogroup.mmolib.api.MMOLineConfig;

public class FishingDropItem {
	private final RandomAmount experience, tugs;
	private final DropItem dropItem;

	private final int weight;

	public FishingDropItem(String value) {
		MMOLineConfig config = new MMOLineConfig(value);

		config.validate("tugs", "experience");

		tugs = new RandomAmount(config.getString("tugs"));
		experience = new RandomAmount(config.getString("experience"));

		weight = config.getInt("weight", 1);
		Validate.isTrue(weight > 0, "A fishing drop table item cannot have 0 weight");

		dropItem = MMOCore.plugin.loadManager.loadDropItem(config);
	}

	public int getWeight() {
		return weight;
	}

	public DropItem getItem() {
		return dropItem;
	}

	public RandomAmount getExperience() {
		return experience;
	}

	public RandomAmount getTugs() {
		return tugs;
	}

	public int rollExperience() {
		return experience.calculateInt();
	}

	public int rollTugs() {
		return tugs.calculateInt();
	}

	public DropItem getDropItem() {
		return dropItem;
	}

	public ItemStack collect(LootBuilder builder) {
		dropItem.collect(builder);
		return builder.getLoot().stream().findAny().get();
	}
}
