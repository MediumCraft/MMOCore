package net.Indyuce.mmocore.api.player;

import java.util.UUID;

import net.Indyuce.mmocore.api.player.profess.PlayerClass;

public abstract class OfflinePlayerData {
	private final UUID uuid;

	public OfflinePlayerData(UUID uuid) {
		this.uuid = uuid;
	}

	public UUID getUniqueId() {
		return uuid;
	}

	public abstract void removeFriend(UUID uuid);

	public abstract boolean hasFriend(UUID uuid);

	public abstract PlayerClass getProfess();

	public abstract int getLevel();

	public abstract long getLastLogin();

	public static OfflinePlayerData get(UUID uuid) {
		return PlayerData.isLoaded(uuid) ? PlayerData.get(uuid) : new SimpleOfflinePlayerData(uuid);
	}
}
