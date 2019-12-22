package net.Indyuce.mmocore.api.player.stats;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.stats.stat.modifier.StatModifier;
import net.Indyuce.mmocore.api.player.stats.stat.modifier.TemporaryStatModifier;

public class PlayerStats {
	private final PlayerData data;

	/*
	 * allows for extra compatibility with extra MMOCore plugins like item
	 * plugins which can apply other stats onto the player.
	 */
	private final Map<String, StatInstance> extra = new HashMap<>();

	public PlayerStats(PlayerData data) {
		this.data = data;
	}

	public PlayerData getData() {
		return data;
	}

	public StatInstance getInstance(StatType stat) {
		if (extra.containsKey(stat.name()))
			return extra.get(stat.name());

		StatInstance ins = new StatInstance(stat);
		extra.put(stat.name(), ins);
		return ins;
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

	public void updateAll() {
		for (StatType stat : StatType.values())
			update(stat);
	}

	public void update(StatType stat) {
		if (stat.hasHandler())
			stat.getHandler().refresh(data, getStat(stat));
	}

	public class StatInstance {
		private final StatType stat;
		private final Map<String, StatModifier> map = new HashMap<>();

		public StatInstance(StatType stat) {
			this.stat = stat;
		}

		public double getTotal() {
			return getTotal(0);
		}

		/*
		 * 1) two types of attributes: flat attributes which add X to the value,
		 * and relative attributes which add X% and which must be applied
		 * afterwards 2) the 'd' parameter lets you choose if the relative
		 * attributes also apply on the base stat, or if they only apply on the
		 * extra stat value
		 */
		public double getTotal(double d) {

			for (StatModifier attr : map.values())
				if (attr.isRelative())
					d = attr.apply(d);

			for (StatModifier attr : map.values())
				if (!attr.isRelative())
					d = attr.apply(d);

			return d;
		}

		public StatModifier getAttribute(String key) {
			return map.get(key);
		}

		public void addModifier(String key, double value) {
			addModifier(key, new StatModifier(value));
		}

		public void applyTemporaryModifier(String key, StatModifier modifier, long duration) {
			addModifier(key, new TemporaryStatModifier(modifier.getValue(), duration, modifier.isRelative(), key, this));
		}

		public void addModifier(String key, StatModifier modifier) {
			map.put(key, modifier);

			update(stat);
		}

		public Set<String> getKeys() {
			return map.keySet();
		}

		public boolean contains(String key) {
			return map.containsKey(key);
		}

		public void remove(String key) {

			/*
			 * closing stat is really important with temporary stats because
			 * otherwise the runnable will try to remove the key from the map
			 * even though the attribute was cancelled before hand
			 */
			if (map.containsKey(key)) {
				map.get(key).close();
				map.remove(key);
			}

			update(stat);
		}

		@Deprecated
		public void setValue(String key, double value) {
			addModifier(key, new StatModifier(value));
		}
	}
}
