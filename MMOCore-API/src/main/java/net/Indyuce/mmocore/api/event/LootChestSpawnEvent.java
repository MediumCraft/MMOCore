package net.Indyuce.mmocore.api.event;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.loot.LootBuilder;
import net.Indyuce.mmocore.loot.chest.LootChest;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class LootChestSpawnEvent extends PlayerDataEvent implements Cancellable {
	private static final HandlerList handlers = new HandlerList();

	private final LootChest chest;
	private final LootBuilder loot;

	private boolean cancelled;

	public LootChestSpawnEvent(PlayerData playerData, LootChest chest, LootBuilder loot) {
		super(playerData);

		this.chest = chest;
		this.loot = loot;
	}

	public LootChest getChest() {
		return chest;
	}

	public LootBuilder getLoot() {
		return loot;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
