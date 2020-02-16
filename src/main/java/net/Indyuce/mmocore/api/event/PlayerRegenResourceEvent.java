package net.Indyuce.mmocore.api.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.resource.PlayerResource;

public class PlayerRegenResourceEvent extends PlayerDataEvent implements Cancellable {
	private static final HandlerList handlers = new HandlerList();

	/*
	 * type of resource being regenerated. this way all three re
	 */
	private final PlayerResource resource;

	/*
	 * amount of resource regenerated. whole point of the event is to be able to
	 * change it.
	 */
	private double amount;

	private boolean cancelled = false;

	public PlayerRegenResourceEvent(PlayerData playerData, PlayerResource resource, double amount) {
		super(playerData);

		this.resource = resource;
		this.amount = amount;
	}

	public PlayerResource getResource() {
		return resource;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
