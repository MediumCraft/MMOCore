package net.Indyuce.mmocore.gui.api.adaptor;

import net.Indyuce.mmocore.gui.api.GeneratedInventory;
import net.Indyuce.mmocore.gui.api.item.InventoryItem;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class Adaptor {
    protected final GeneratedInventory generated;

    public Adaptor(GeneratedInventory generated) {
        this.generated = generated;
    }

    public abstract void open();

    public abstract void close();

    /**
     * Applies a modification to an item in a new asynchronous thread.
     * This is mostly used to apply owners to skulls as this creates a
     * request to the Minecraft servers to get the player head.
     *
     * @param item   Inventory item
     * @param n      Inventory item index
     * @param placed Item already generated
     * @param update What to do with the item
     */
    public abstract void asyncUpdate(InventoryItem<?> item, int n, ItemStack placed, Consumer<ItemStack> update);

    /**
     * Applies a modification after the given future is complete.
     *
     * @param future Future being completed
     * @param item   Inventory item
     * @param n      Inventory item index
     * @param placed Item already generated
     * @param update What to do with the item
     */
    public abstract <T> void asyncUpdate(CompletableFuture<T> future, InventoryItem<?> item, int n, ItemStack placed, BiConsumer<T, ItemStack> update);
}
