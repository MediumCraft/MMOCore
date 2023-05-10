package net.Indyuce.mmocore.util.item;

import io.lumine.mythic.lib.api.item.NBTItem;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class SimpleItemBuilder extends AbstractItemBuilder {
    public SimpleItemBuilder(@NotNull ConfigItem configItem) {
        super(configItem);
    }

    public SimpleItemBuilder(@NotNull String key) {
        super(key);
    }

    @Override
    public void whenBuildingMeta(ItemStack item, ItemMeta meta) {
        // Nothing
    }

    @Override
    public void whenBuildingNBT(NBTItem nbtItem) {
        // Nothing
    }
}
