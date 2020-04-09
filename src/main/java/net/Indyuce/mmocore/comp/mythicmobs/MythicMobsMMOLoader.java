package net.Indyuce.mmocore.comp.mythicmobs;

import org.bukkit.configuration.ConfigurationSection;

import net.Indyuce.mmocore.api.experience.Profession;
import net.Indyuce.mmocore.api.experience.source.type.ExperienceSource;
import net.Indyuce.mmocore.api.load.MMOLoader;
import net.Indyuce.mmocore.api.quest.objective.Objective;
import net.Indyuce.mmocore.api.quest.trigger.Trigger;
import net.Indyuce.mmocore.comp.mythicmobs.load.KillMythicMobExperienceSource;
import net.Indyuce.mmocore.comp.mythicmobs.load.KillMythicMobObjective;
import net.Indyuce.mmocore.comp.mythicmobs.load.MythicMobSkillTrigger;
import net.mmogroup.mmolib.api.MMOLineConfig;

public class MythicMobsMMOLoader extends MMOLoader {

	@Override
	public Trigger loadTrigger(MMOLineConfig config) {

		if (config.getKey().equalsIgnoreCase("mmskill") || config.getKey().equalsIgnoreCase("mythicmobskill"))
			return new MythicMobSkillTrigger(config);

		return null;
	}

	@Override
	public Objective loadObjective(MMOLineConfig config, ConfigurationSection section) {

		if (config.getKey().equalsIgnoreCase("killmythicmob"))
			return new KillMythicMobObjective(section, config);

		return null;
	}

	@Override
	public ExperienceSource<?> loadExperienceSource(MMOLineConfig config, Profession profession) {

		if (config.getKey().equalsIgnoreCase("killmythicmob"))
			return new KillMythicMobExperienceSource(profession, config);

		return null;
	}
}
