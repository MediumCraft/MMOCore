package net.Indyuce.mmocore.api.event;

import org.bukkit.event.HandlerList;

import net.Indyuce.mmocore.api.player.PlayerData;

public class PlayerCombatEvent extends PlayerDataEvent {
	private static final HandlerList handlers = new HandlerList();

	private final boolean enter;

	public PlayerCombatEvent(PlayerData playerData, boolean enter) {
		super(playerData);

		this.enter = enter;
	}

	public boolean entersCombat() {
		return enter;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
