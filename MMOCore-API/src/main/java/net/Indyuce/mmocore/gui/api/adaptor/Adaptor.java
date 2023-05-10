package net.Indyuce.mmocore.gui.api.adaptor;

import net.Indyuce.mmocore.gui.api.GeneratedInventory;
import net.Indyuce.mmocore.gui.api.item.InventoryItem;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public abstract class Adaptor {
    protected final GeneratedInventory generated;

    public Adaptor(GeneratedInventory generated) {
        this.generated = generated;
    }

    public abstract void open();

    public abstract void close();

    @Deprecated
    public abstract void dynamicallyUpdateItem(InventoryItem<?> item, int n, ItemStack placed, Consumer<ItemStack> update);
}
