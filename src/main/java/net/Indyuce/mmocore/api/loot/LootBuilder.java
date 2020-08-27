package net.Indyuce.mmocore.api.loot;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmocore.api.player.PlayerData;

public class LootBuilder {
	private final PlayerData player;
	private final List<ItemStack> loot = new ArrayList<>();

	private double capacity;

	/**
	 * Used to create loot from a drop table
	 * 
	 * @param player
	 *            Player looting
	 * @param capacity
	 *            Capacity is the maximum amount of item weight generated using
	 *            this table. If capacity is set to 10, this table cannot drop
	 *            an item with 5 weight and another with 6 weight at the saeme
	 *            time.
	 */
	public LootBuilder(PlayerData player, double capacity) {
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
