package net.Indyuce.mmocore.api.player.social;

import java.util.UUID;

public abstract class Request {
	private UUID uuid = UUID.randomUUID();
	private long date = System.currentTimeMillis();

	public UUID getUniqueId() {
		return uuid;
	}

	public boolean isTimedOut() {
		return date + 1000 * 60 * 2 < System.currentTimeMillis();
	}

	public abstract void accept();

	public abstract void deny();
}
