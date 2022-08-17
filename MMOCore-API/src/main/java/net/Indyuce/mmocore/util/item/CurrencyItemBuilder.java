package net.Indyuce.mmocore.util.item;

import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.NBTItem;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CurrencyItemBuilder extends AbstractItemBuilder {
    private final int worth;

    public CurrencyItemBuilder(String key, int worth) {
        super(key);

        addPlaceholders("worth", String.valueOf(this.worth = worth));
    }

    @Override
    public void whenBuildingMeta(ItemStack item, ItemMeta meta) {
        // Nothing
    }

    @Override
    public void whenBuildingNBT(NBTItem nbtItem) {
        nbtItem.addTag(new ItemTag("RpgWorth", worth));
    }
}
