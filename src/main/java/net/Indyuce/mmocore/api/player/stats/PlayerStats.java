package net.Indyuce.mmocore.api.player.stats;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.stat.StatInstance;
import net.mmogroup.mmolib.api.stat.StatMap;
import net.mmogroup.mmolib.api.stat.modifier.StatModifier;

public class PlayerStats {
	private final PlayerData data;

	/**
	 * Utilclass to easily manipulate the MMOLib stat map
	 * 
	 * @param data
	 */
	public PlayerStats(PlayerData data) {
		this.data = data;
	}

	public PlayerData getData() {
		return data;
	}

	public StatMap getMap() {
		return data.getMMOPlayerData().getStatMap();
	}

	public StatInstance getInstance(StatType stat) {
		return getMap().getInstance(stat.name());
	}

	public StatInstance getInstance(String stat) {
		return getMap().getInstance(stat);
	}

	/*
	 * applies relative attributes on the base stat too
	 */
	public double getStat(StatType stat) {
		return getInstance(stat).getTotal();
	}

	public double getBase(StatType stat) {
		return data.getProfess().calculateStat(stat,
				stat.hasProfession() ? data.getCollectionSkills().getLevel(stat.getProfession()) : data.getLevel());
	}

	/*
	 * used to update MMOCore stat modifiers due to class and send them over to
	 * MMOLib. must be ran everytime the player levels up or changes class.
	 */
	public synchronized void updateStats() {
		getMap().getInstances().forEach(ins -> ins.removeIf(key -> key.equals("mmocoreClass")));

		for (StatType stat : StatType.values()) {
			StatInstance instance = getMap().getInstance(stat.name());
			double total = getBase(stat) - instance.getBase();

			if (total != 0)
				instance.addModifier("mmocoreClass", new StatModifier(total));
		}

		MMOLib.plugin.getStats().runUpdates(getMap());
	}
}
