package net.Indyuce.mmocore.comp.citizens;

import org.bukkit.configuration.ConfigurationSection;

import net.Indyuce.mmocore.api.droptable.condition.Condition;
import net.Indyuce.mmocore.api.droptable.dropitem.DropItem;
import net.Indyuce.mmocore.api.experience.Profession;
import net.Indyuce.mmocore.api.experience.source.type.ExperienceSource;
import net.Indyuce.mmocore.api.load.MMOLineConfig;
import net.Indyuce.mmocore.api.load.MMOLoader;
import net.Indyuce.mmocore.api.quest.objective.Objective;
import net.Indyuce.mmocore.api.quest.trigger.Trigger;

public class CitizensMMOLoader implements MMOLoader {

	@Override
	public Condition loadCondition(MMOLineConfig config) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Trigger loadTrigger(MMOLineConfig config) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DropItem loadDropItem(MMOLineConfig config) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Objective loadObjective(MMOLineConfig config, ConfigurationSection section) {

		if (config.getKey().equals("talkto"))
			return new TalktoCitizenObjective(section, config);

		if (config.getKey().equals("getitem"))
			return new GetItemObjective(section, config);

		return null;
	}

	@Override
	public ExperienceSource<?> loadExperienceSource(MMOLineConfig config, Profession profession) {
		// TODO Auto-generated method stub
		return null;
	}
}
