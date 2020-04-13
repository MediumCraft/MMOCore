package net.Indyuce.mmocore.api.loot;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmocore.api.player.PlayerData;

public class LootBuilder {
	private final PlayerData player;
	private final List<ItemStack> loot = new ArrayList<>();

	private double capacity;

	/*
	 * instance which saves what entity is currently rolling a loot table and
	 * how much item capacity the table has left
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
