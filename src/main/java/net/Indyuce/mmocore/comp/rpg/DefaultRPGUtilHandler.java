package net.Indyuce.mmocore.comp.rpg;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.stats.TemporaryStats;

public class DefaultRPGUtilHandler implements RPGUtilHandler {
	@Override
	public TemporaryStats cachePlayerStats(PlayerData playerData) {
		return new TemporaryStats(playerData.getStats());
	}
}
