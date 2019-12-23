package net.Indyuce.mmocore.comp.rpg;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.stats.PlayerStats.CachedStats;

public interface RPGUtilHandler {
	public CachedStats cachePlayerStats(PlayerData playerData);
}
