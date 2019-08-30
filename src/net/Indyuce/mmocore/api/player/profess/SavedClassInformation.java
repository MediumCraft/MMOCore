package net.Indyuce.mmocore.api.player.profess;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.PlayerClass.ClassOption;
import net.Indyuce.mmocore.api.skill.Skill;

public class SavedClassInformation {
	private final int level, experience, skillPoints;
	private final Map<String, Integer> skills;

	public SavedClassInformation(ConfigurationSection config) {
		level = config.getInt("level");
		experience = config.getInt("experience");
		skillPoints = config.getInt("skill-points");

		skills = new HashMap<>();
		if (config.contains("skill"))
			config.getKeys(false).forEach(key -> skills.put(key, config.getInt(key)));
	}

	public SavedClassInformation(PlayerData player) {
		level = player.getLevel();
		skillPoints = player.getSkillPoints();
		experience = player.getExperience();
		skills = player.mapSkillLevels();
	}

	public SavedClassInformation(int level, int experience, int skillPoints) {
		this.level = level;
		this.experience = experience;
		this.skillPoints = skillPoints;
		skills = new HashMap<>();
	}

	public int getLevel() {
		return level;
	}

	public int getExperience() {
		return experience;
	}

	public int getSkillPoints() {
		return skillPoints;
	}

	public Set<String> getSkillKeys() {
		return skills.keySet();
	}

	public int getSkillLevel(Skill skill) {
		return getSkillLevel(skill.getId());
	}

	public int getSkillLevel(String id) {
		return skills.get(id);
	}

	public void registerSkillLevel(Skill skill, int level) {
		registerSkillLevel(skill.getId(), level);
	}

	public void registerSkillLevel(String skill, int level) {
		skills.put(skill, level);
	}

	public void load(PlayerClass profess, PlayerData player) {

		/*
		 * saves current class info inside a SavedClassInformation, only if the
		 * class is a real class and not the default one.
		 */
		if (!player.getProfess().hasOption(ClassOption.DEFAULT))
			player.applyClassInfo(player.getProfess(), new SavedClassInformation(player));

		/*
		 * resets information which much be reset after everything is saved.
		 */
		player.clearSkillLevels();
		while (player.hasSkillBound(0))
			player.unbindSkill(0);

		/*
		 * reads this class info, applies it to the player. set class after
		 * changing level so the player stats can be calculated based on new
		 * level.
		 */
		player.setLevel(level);
		player.setExperience(experience);
		player.setSkillPoints(skillPoints);
		skills.keySet().forEach(id -> player.setSkillLevel(id, skills.get(id)));

		/*
		 * unload current class information and set the new profess once
		 * everything is changed
		 */
		player.setClass(profess);
		player.unloadClassInfo(profess);
	}
}
