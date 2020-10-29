package net.Indyuce.mmocore.api.skill;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.skill.Skill.SkillInfo;
import net.Indyuce.mmocore.comp.mythicmobs.MythicMobSkill;

public class PlayerSkillData {
	private final Map<String, Long> cooldowns = new HashMap<>();
	private final PlayerData data;

	/*
	 * MythicMobs skill damage is handled via math formula which can retrieve
	 * PAPI placeholders. when a skill is cast, all skill modifiers are cached
	 * into that map: 1- for easier and faster access 2- it removes interference
	 * for example when stats are calculating not when the spell is cast but
	 * rather when the spell hits
	 */
	private final Map<String, CachedModifier> cache = new HashMap<>();

	// public int ambers;

	public PlayerSkillData(PlayerData data) {
		this.data = data;
	}

	/*
	 * any method which returns long RETURNS milliseconds (cooldowns are either
	 * stored in double when it's the actual value or in long when it's precise
	 * up to 3 digits)
	 */
	public long getCooldown(SkillInfo skill) {
		return Math.max(0, lastCast(skill.getSkill()) - System.currentTimeMillis() + (long) (1000. * skill.getModifier("cooldown", data.getSkillLevel(skill.getSkill()))));
	}

	public long lastCast(Skill skill) {
		return cooldowns.containsKey(skill.getId()) ? cooldowns.get(skill.getId()) : 0;
	}

	public void setLastCast(Skill skill) {
		setLastCast(skill, System.currentTimeMillis());
	}

	public void setLastCast(Skill skill, long ms) {
		cooldowns.put(skill.getId(), ms);
	}

	public void reduceCooldown(SkillInfo skill, double value, boolean relative) {
		cooldowns.put(skill.getSkill().getId(), lastCast(skill.getSkill()) + (long) (relative ? value * getCooldown(skill) : value * 1000));
	}

	// public void resetData() {
	// ambers = 0;
	// }

	public double getCachedModifier(String name) {
		return cache.containsKey(name) ? cache.get(name).getValue() : 0;
	}

	public void cacheModifiers(MythicMobSkill mmSkill, SkillResult cast) {
		for (String modifier : cast.getSkill().getModifiers())
			cacheModifier(mmSkill, modifier, cast.getModifier(modifier));

		cacheModifier(mmSkill, "level", cast.getLevel());
	}

	public void cacheModifiers(String skill, SkillResult cast) {
		for (String modifier : cast.getSkill().getModifiers())
			cacheModifier(skill, modifier, cast.getModifier(modifier));

		cacheModifier(skill, "level", cast.getLevel());
	}

	public void cacheModifier(MythicMobSkill skill, String name, double value) {
		cacheModifier(skill.getInternalName(), name, value);
	}
	
	public void cacheModifier(String skill, String name, double value) {
		cache.put(skill + "." + name, new CachedModifier(value));
	}

	public void refresh() {
		for (Iterator<CachedModifier> iterator = cache.values().iterator(); iterator.hasNext();)
			if (iterator.next().isTimedOut())
				iterator.remove();
	}

	public class CachedModifier {
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
