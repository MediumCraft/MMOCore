package net.Indyuce.mmocore.comp.rpg;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.stats.PlayerStats.CachedStats;

public class DefaultRPGUtilHandler implements RPGUtilHandler {

	@Override
	public CachedStats cachePlayerStats(PlayerData playerData) {
		return playerData.getStats().cache();
	}
}
