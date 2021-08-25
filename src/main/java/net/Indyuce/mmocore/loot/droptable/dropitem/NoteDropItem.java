package net.Indyuce.mmocore.loot.droptable.dropitem;

import net.Indyuce.mmocore.loot.LootBuilder;
import net.Indyuce.mmocore.api.util.item.CurrencyItem;
import io.lumine.mythic.lib.api.MMOLineConfig;

public class NoteDropItem extends DropItem {
	private final int min, max;

	public NoteDropItem(MMOLineConfig config) {
		super(config);

		config.validate("max", "min");

		min = (int) config.getDouble("min");
		max = (int) config.getDouble("max");
	}

	@Override
	public void collect(LootBuilder builder) {
		builder.addLoot(new CurrencyItem("NOTE", random.nextInt(max - min + 1) + min, rollAmount()).build());
	}
}
