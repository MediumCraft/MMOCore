package net.Indyuce.mmocore.loot;

import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class LootBuilder {
    private final PlayerData player;
    private final List<ItemStack> loot = new ArrayList<>();

    private double capacity;

    public static double DEFAULT_CAPACITY = 100;

    public LootBuilder(PlayerData player) {
        this(player, DEFAULT_CAPACITY);
    }

    /**
     * Used to create loot from a drop table
     *
     * @param player   Player looting
     * @param capacity Capacity is the maximum amount of item weight generated using
     *                 this table. If capacity is set to 10, this table cannot drop
     *                 an item with 5 weight and another with 6 weight at the saeme
     *                 time.
     */
    public LootBuilder(@NotNull PlayerData player, double capacity) {
        this.player = player;
        this.capacity = capacity;
    }

    public PlayerData getEntity() {
        return player;
    }

    public List<ItemStack> getLoot() {
        return loot;
    }

    public double getCapacity() {
        return capacity;
    }

    public void addLoot(ItemStack item) {
        loot.add(item);
    }

    public void addLoot(List<? extends ItemStack> items) {
        loot.addAll(items);
    }

    public void reduceCapacity(double value) {
        this.capacity = Math.max(0, capacity - value);
    }
}
