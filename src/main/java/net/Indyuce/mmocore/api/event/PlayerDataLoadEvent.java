package net.Indyuce.mmocore.api.event;

import org.bukkit.event.HandlerList;

import net.Indyuce.mmocore.api.player.PlayerData;

public class PlayerDataLoadEvent extends PlayerDataEvent {
	private static final HandlerList handlers = new HandlerList();

	public PlayerDataLoadEvent(PlayerData playerData) {
		super(playerData);
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
