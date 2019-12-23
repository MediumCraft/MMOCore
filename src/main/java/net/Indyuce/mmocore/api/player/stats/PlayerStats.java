package net.Indyuce.mmocore.api.player.stats;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.AttackResult;
import net.mmogroup.mmolib.api.DamageType;
import net.mmogroup.mmolib.api.stat.StatMap;
import net.mmogroup.mmolib.api.stat.StatMap.StatInstance;

public class PlayerStats {
	private final PlayerData data;
	private final StatMap map;

	/*
	 * util class to manipulate more easily stat data from MMOLib
	 */
	public PlayerStats(PlayerData data) {
		this.data = data;

		/*
		 * retrieve stat map (where all stat data is saved) and refresh MMOCore
		 * data
		 */
		this.map = StatMap.get(data.getPlayer());
		map.getPlayerData().setMMOCore(data);
	}

	public PlayerData getData() {
		return data;
	}

	public StatMap getMap() {
		return map;
	}

	public StatInstance getInstance(StatType stat) {
		return map.getInstance(stat.name());
	}

	/*
	 * applies relative attributes on the base stat too
	 */
	public double getStat(StatType stat) {
		return getInstance(stat).getTotal(getBase(stat));
	}

	public double getBase(StatType stat) {
		return data.getProfess().calculateStat(stat, stat.hasProfession() ? data.getCollectionSkills().getLevel(stat.getProfession()) : data.getLevel());
	}

	/*
	 * applies relative attributes on the extra stat value only
	 */
	public double getExtraStat(StatType stat) {
		return getInstance(stat).getTotal(0);
	}

	public CachedStats cache() {
		return new CachedStats();
	}

	public class CachedStats {
		private final Player player;

		private final Map<String, Double> stats = new HashMap<>();

		public CachedStats() {
			this.player = data.getPlayer();
			for (StatType stat : StatType.values())
				this.stats.put(stat.name(), getStat(stat));
		}

		public Player getPlayer() {
			return player;
		}

		public PlayerData getData() {
			return data;
		}

		public double getStat(StatType stat) {
			return stats.get(stat.name());
		}

		public void damage(LivingEntity target, double value, DamageType... types) {
			MMOLib.plugin.getDamage().damage(data.getPlayer(), target, new AttackResult(true, value, types));
		}
	}
}
