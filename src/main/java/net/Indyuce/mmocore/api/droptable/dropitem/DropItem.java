package net.Indyuce.mmocore.api.droptable.dropitem;

import java.util.List;
import java.util.Random;

import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmocore.api.util.math.formula.RandomAmount;
import net.mmogroup.mmolib.api.MMOLineConfig;

public abstract class DropItem {
	protected static final Random random = new Random();

	private double chance;
	private RandomAmount amount;

	public DropItem(MMOLineConfig config) {
		chance = config.args().length > 0 ? Double.parseDouble(config.args()[0]) : 1;
		amount = config.args().length > 1 ? new RandomAmount(config.args()[1]) : new RandomAmount(1, 0);
	}

	public RandomAmount getAmount() {
		return amount;
	}

	public double getChance() {
		return chance;
	}

	public int rollAmount() {
		return amount.calculateInt();
	}

	public boolean rollChance() {
		return random.nextDouble() < chance;
	}

	public abstract void collect(List<ItemStack> total);
}
