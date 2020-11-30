package net.Indyuce.mmocore.api.skill;

import java.util.HashMap;
import java.util.Map;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.skill.Skill.SkillInfo;
import net.Indyuce.mmocore.comp.mythicmobs.skill.MythicMobSkill;

/**
 * Note: any method which return longs returns milliseconds.
 * 
 * @author cympe
 */
public class PlayerSkillData {
	private final Map<String, Long> cooldowns = new HashMap<>();
	private final PlayerData data;

	/**
	 * MythicMobs skill damage is handled via math formula which can retrieve
	 * PAPI placeholders. When a skill is cast, all skill modifiers are cached
	 * into that map: 1- for easier and faster access 2- it removes interference
	 * for example when stats are calculating not when the spell is cast but
	 * rather when the spell hits
	 */
	private final Map<String, CachedModifier> cache = new HashMap<>();

	public PlayerSkillData(PlayerData data) {
		this.data = data;
	}

	public long getCooldown(SkillInfo skill) {
		return Math.max(0, lastCast(skill.getSkill()) - System.currentTimeMillis()
				+ (long) (1000. * skill.getModifier("cooldown", data.getSkillLevel(skill.getSkill()))));
	}

	/**
	 * @param skill
	 *            Skill that was cast
	 * @return Last time stamp the skill was cast or 0 if never
	 */
	public long lastCast(Skill skill) {
		return cooldowns.containsKey(skill.getId()) ? cooldowns.get(skill.getId()) : 0;
	}

	/**
	 * Sets the last time the player cast the skill at current time
	 * 
	 * @param skill
	 *            Skill being cast
	 */
	public void setLastCast(Skill skill) {
		setLastCast(skill, System.currentTimeMillis());
	}

	/**
	 * Sets the last time the player cast the skill at given time
	 * 
	 * @param ms
	 *            Time stammp
	 * @param skill
	 *            Skill being cast
	 */
	public void setLastCast(Skill skill, long ms) {
		cooldowns.put(skill.getId(), ms);
	}

	/**
	 * Reduces the remaining cooldown of a specific skill
	 * 
	 * @param skill
	 *            Skill cast
	 * @param value
	 *            Amount of skill cooldown instant reduction.
	 * @param relative
	 *            If the cooldown reduction is relative to the remaining
	 *            cooldown. If set to true, instant reduction is equal to
	 *            (value) * (skill cooldown). If set to false, instant reduction
	 *            is the given flat value
	 */
	public void reduceCooldown(SkillInfo skill, double value, boolean relative) {
		long reduction = (long) (relative ? value * (double) getCooldown(skill) : value * 1000.);
		cooldowns.put(skill.getSkill().getId(), lastCast(skill.getSkill()) + reduction);
	}

	public double getCachedModifier(String name) {
		return cache.containsKey(name) ? cache.get(name).getValue() : 0;
	}

	public void cacheModifiers(MythicMobSkill skill, SkillResult cast) {
		cacheModifiers(skill.getInternalName(), cast);
	}

	/**
	 * Caches all modifiers from a cast skill in the map
	 * 
	 * @param skill
	 *            Skill identifier being used as reference in the map
	 * @param cast
	 *            Skill being cast
	 */
	public void cacheModifiers(String skill, SkillResult cast) {
		for (String modifier : cast.getSkill().getModifiers())
			cacheModifier(skill, modifier, cast.getModifier(modifier));

		cacheModifier(skill, "level", cast.getLevel());
	}

	/**
	 * Caches a specific modifier
	 * 
	 * @param skill
	 *            The identifier of the skill being cast
	 * @param name
	 *            Modifier name
	 * @param value
	 *            Modifier value
	 */
	public void cacheModifier(String skill, String name, double value) {
		cache.put(skill + "." + name, new CachedModifier(value));
	}

	/**
	 * Empties cached modifiers. Modifiers should time out one minute after the
	 * skill was cast
	 */
	public void refresh() {
		cache.values().removeIf(CachedModifier::isTimedOut);
	}

	public static class CachedModifier {
		private final long date = System.currentTimeMillis();
		private final double value;

		public CachedModifier(double value) {
			this.value = value;
		}

		public boolean isTimedOut() {
			return date + 1000 * 60 < System.currentTimeMillis();
		}

		public double getValue() {
			return value;
		}
	}
}
