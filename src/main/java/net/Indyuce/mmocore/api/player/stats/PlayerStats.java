package net.Indyuce.mmocore.api.player.stats;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.mmogroup.mmolib.api.player.MMOData;
import net.mmogroup.mmolib.api.stat.StatInstance;
import net.mmogroup.mmolib.api.stat.StatMap;

public class PlayerStats {
	private final PlayerData data;
	private final StatMap map;

	/*
	 * util class to manipulate more easily stat data from MMOLib
	 */
	public PlayerStats(PlayerData data) {
		this.data = data;

		map = MMOData.get(data.getPlayer()).setMMOCore(data).getStatMap();
	}

	public PlayerData getData() {
		return data;
	}

	public StatMap getMap() {
		return map;
	}

	public StatInstance getInstance(StatType stat) {
		return getInstance(stat.name());
	}

	public StatInstance getInstance(String stat) {
		return map.getInstance(stat);
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
}
