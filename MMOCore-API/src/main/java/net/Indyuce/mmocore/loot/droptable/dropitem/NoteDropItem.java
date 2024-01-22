package net.Indyuce.mmocore.loot.droptable.dropitem;

import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.util.item.CurrencyItemBuilder;
import net.Indyuce.mmocore.loot.LootBuilder;
import org.bukkit.inventory.ItemStack;

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
        ItemStack item = new CurrencyItemBuilder("NOTE", random.nextInt(max - min + 1) + min).build();
        item.setAmount(rollAmount());
        builder.addLoot(item);
    }
}
