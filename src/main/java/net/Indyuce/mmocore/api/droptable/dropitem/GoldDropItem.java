package net.Indyuce.mmocore.api.droptable.dropitem;

import java.util.List;

import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmocore.api.util.item.CurrencyItem;
import net.Indyuce.mmoitems.api.util.MMOLineConfig;

public class GoldDropItem extends DropItem {
	public GoldDropItem(MMOLineConfig config) {
		super(config);
	}

	@Override
	public void collect(List<ItemStack> total) {
		total.add(new CurrencyItem("GOLD_COIN", 1, rollAmount()).build());
	}
}
