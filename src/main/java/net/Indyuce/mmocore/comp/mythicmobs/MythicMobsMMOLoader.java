package net.Indyuce.mmocore.comp.mythicmobs;

import net.Indyuce.mmocore.experience.provider.ExperienceDispenser;
import org.bukkit.configuration.ConfigurationSection;

import net.Indyuce.mmocore.experience.Profession;
import net.Indyuce.mmocore.experience.source.type.ExperienceSource;
import net.Indyuce.mmocore.api.load.MMOLoader;
import net.Indyuce.mmocore.api.quest.objective.Objective;
import net.Indyuce.mmocore.api.quest.trigger.Trigger;
import net.Indyuce.mmocore.comp.mythicmobs.load.KillMythicFactionExperienceSource;
import net.Indyuce.mmocore.comp.mythicmobs.load.KillMythicFactionObjective;
import net.Indyuce.mmocore.comp.mythicmobs.load.KillMythicMobExperienceSource;
import net.Indyuce.mmocore.comp.mythicmobs.load.KillMythicMobObjective;
import net.Indyuce.mmocore.comp.mythicmobs.load.MythicMobSkillTrigger;
import io.lumine.mythic.lib.api.MMOLineConfig;

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
		if (config.getKey().equalsIgnoreCase("killmythicfaction"))
			return new KillMythicFactionObjective(section, config);

		return null;
	}

	@Override
	public ExperienceSource<?> loadExperienceSource(MMOLineConfig config, ExperienceDispenser dispenser) {

		if (config.getKey().equalsIgnoreCase("killmythicmob"))
			return new KillMythicMobExperienceSource(dispenser, config);
		if (config.getKey().equalsIgnoreCase("killmythicfaction"))
			return new KillMythicFactionExperienceSource(dispenser, config);

		return null;
	}
}
