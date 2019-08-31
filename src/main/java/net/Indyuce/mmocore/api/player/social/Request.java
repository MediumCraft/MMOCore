package net.Indyuce.mmocore.api.player.social;

import java.util.UUID;

import net.Indyuce.mmocore.api.player.PlayerData;

public abstract class Request {
	private final UUID uuid = UUID.randomUUID();
	private final long date = System.currentTimeMillis();
	private final PlayerData creator;

	public Request(PlayerData creator) {
		this.creator = creator;
	}

	public PlayerData getCreator() {
		return creator;
	}

	public UUID getUniqueId() {
		return uuid;
	}

	public boolean isTimedOut() {
		return date + 1000 * 60 * 2 < System.currentTimeMillis();
	}

	public abstract void accept();

	public abstract void deny();
}
