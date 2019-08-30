package net.Indyuce.mmocore.api.event.social;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import net.Indyuce.mmocore.api.event.PlayerDataEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.social.Party;

public class PartyChatEvent extends PlayerDataEvent implements Cancellable {
	private static final HandlerList handlers = new HandlerList();

	private final Party party;

	private boolean cancelled;
	private String message;

	public PartyChatEvent(PlayerData playerData, String message) {
		super(playerData);
		this.party = playerData.getParty();
		this.message = message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public Party getParty() {
		return party;
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
