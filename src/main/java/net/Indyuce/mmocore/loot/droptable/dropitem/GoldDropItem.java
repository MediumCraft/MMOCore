package net.Indyuce.mmocore.loot.droptable.dropitem;

import net.Indyuce.mmocore.loot.LootBuilder;
import net.Indyuce.mmocore.api.util.item.CurrencyItem;
import io.lumine.mythic.lib.api.MMOLineConfig;

public class GoldDropItem extends DropItem {
	public GoldDropItem(MMOLineConfig config) {
		super(config);
	}

	@Override
	public void collect(LootBuilder builder) {
		builder.addLoot(new CurrencyItem("GOLD_COIN", 1, rollAmount()).build());
	}
}
