package net.Indyuce.mmocore.api.event;

import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.event.player.PlayerEvent;

public abstract class PlayerDataEvent extends PlayerEvent {
	private final PlayerData playerData;

	public PlayerDataEvent(PlayerData playerData) {
		super(playerData.getPlayer());
		
		this.playerData = playerData;
	}

	public PlayerData getData() {
		return playerData;
	}
}
