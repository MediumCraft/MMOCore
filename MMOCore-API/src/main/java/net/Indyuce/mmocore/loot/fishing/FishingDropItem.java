package net.Indyuce.mmocore.loot.fishing;

import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.loot.Weighted;
import net.Indyuce.mmocore.api.util.math.formula.RandomAmount;
import net.Indyuce.mmocore.loot.LootBuilder;
import net.Indyuce.mmocore.loot.droptable.dropitem.DropItem;
import org.apache.commons.lang.Validate;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class FishingDropItem implements Weighted {
	private final RandomAmount experience, tugs;
	private final DropItem dropItem;

	public FishingDropItem(MMOLineConfig config) {
		config.validate("tugs", "experience");

		tugs = new RandomAmount(config.getString("tugs"));
		experience = new RandomAmount(config.getString("experience"));

		dropItem = MMOCore.plugin.loadManager.loadDropItem(config);
		Validate.isTrue(dropItem.getWeight() > 0, "A fishing drop table item must have a strictly positive weight");
	}

	@Override
	public double getWeight() {
		return dropItem.getWeight();
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

	@Nullable
	public ItemStack collect(LootBuilder builder) {
		dropItem.collect(builder);
		return builder.getLoot().stream().findAny().orElse(null);
	}
}
