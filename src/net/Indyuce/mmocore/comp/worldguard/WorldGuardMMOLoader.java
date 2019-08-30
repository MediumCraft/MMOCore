package net.Indyuce.mmocore.comp.worldguard;

import org.bukkit.configuration.ConfigurationSection;

import net.Indyuce.mmocore.api.droptable.condition.Condition;
import net.Indyuce.mmocore.api.droptable.dropitem.DropItem;
import net.Indyuce.mmocore.api.experience.Profession;
import net.Indyuce.mmocore.api.experience.source.type.ExperienceSource;
import net.Indyuce.mmocore.api.load.MMOLineConfig;
import net.Indyuce.mmocore.api.load.MMOLoader;
import net.Indyuce.mmocore.api.quest.objective.Objective;
import net.Indyuce.mmocore.api.quest.trigger.Trigger;

public class WorldGuardMMOLoader implements MMOLoader {

	@Override
	public Condition loadCondition(MMOLineConfig config) {

		if (config.getKey().equals("region"))
			return new RegionCondition(config);

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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ExperienceSource<?> loadExperienceSource(MMOLineConfig config, Profession profession) {
		// TODO Auto-generated method stub
		return null;
	}
}
