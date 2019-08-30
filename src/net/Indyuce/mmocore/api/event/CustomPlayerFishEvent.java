package net.Indyuce.mmocore.api.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import net.Indyuce.mmocore.api.droptable.dropitem.DropItem;
import net.Indyuce.mmocore.api.player.PlayerData;

public class CustomPlayerFishEvent extends PlayerDataEvent implements Cancellable {
	private static final HandlerList handlers = new HandlerList();

	private final DropItem caught;

	private boolean cancelled = false;

	public CustomPlayerFishEvent(PlayerData player, DropItem caught) {
		super(player);
		
		this.caught = caught;
	}

	public DropItem getCaught() {
		return caught;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean value) {
		cancelled = value;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
