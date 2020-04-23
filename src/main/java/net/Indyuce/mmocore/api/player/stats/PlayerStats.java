package net.Indyuce.mmocore.api.player.stats;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.mmogroup.mmolib.api.player.MMOData;
import net.mmogroup.mmolib.api.stat.StatInstance;
import net.mmogroup.mmolib.api.stat.StatMap;
import net.mmogroup.mmolib.api.stat.modifier.StatModifier;

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
	public void updateStats() {
		map.getInstances().forEach(ins -> ins.removeIf(key -> key.equals("mmocoreClass")));

		for (StatType stat : StatType.values()) {
			double base = getBase(stat);
			if (base == 0)
				continue;

			StatInstance instance = map.getInstance(stat.name());
			if ((base -= instance.getVanilla()) != 0)
				instance.addModifier("mmocoreClass", new StatModifier(base));
		}

		map.updateAll();
	}
}
