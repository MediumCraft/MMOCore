package net.Indyuce.mmocore.loot.droptable.dropitem;

import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.util.item.CurrencyItemBuilder;
import net.Indyuce.mmocore.loot.LootBuilder;
import org.bukkit.inventory.ItemStack;

public class GoldDropItem extends DropItem {
    public GoldDropItem(MMOLineConfig config) {
        super(config);
    }

    @Override
    public void collect(LootBuilder builder) {
        ItemStack item = new CurrencyItemBuilder("GOLD_COIN", 1).build();
        item.setAmount(rollAmount());
        builder.addLoot(item);
    }
}
