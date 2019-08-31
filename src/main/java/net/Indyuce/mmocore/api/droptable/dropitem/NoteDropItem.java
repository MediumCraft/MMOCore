package net.Indyuce.mmocore.api.droptable.dropitem;

import java.util.List;

import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmocore.api.item.CurrencyItem;
import net.Indyuce.mmocore.api.load.MMOLineConfig;

public class NoteDropItem extends DropItem {
	private int min, max;

	public NoteDropItem(MMOLineConfig config) {
		super(config);

		config.validate("max", "min");

		min = (int) config.getDouble("min");
		max = (int) config.getDouble("max");
	}

	@Override
	public void collect(List<ItemStack> total) {
		total.add(new CurrencyItem("NOTE", random.nextInt(max - min + 1) + min, rollAmount()).build());
	}
}
