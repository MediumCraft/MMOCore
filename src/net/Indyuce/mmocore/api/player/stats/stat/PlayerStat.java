package net.Indyuce.mmocore.api.player.stats.stat;

import net.Indyuce.mmocore.api.player.PlayerData;

public abstract class PlayerStat {
	private final String id;

	public PlayerStat(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}
	
	public abstract void refresh(PlayerData player, double val);
}
