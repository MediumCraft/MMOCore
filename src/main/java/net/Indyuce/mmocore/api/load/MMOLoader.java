package net.Indyuce.mmocore.api.load;

import org.bukkit.configuration.ConfigurationSection;

import net.Indyuce.mmocore.api.droptable.condition.Condition;
import net.Indyuce.mmocore.api.droptable.dropitem.DropItem;
import net.Indyuce.mmocore.api.experience.Profession;
import net.Indyuce.mmocore.api.experience.source.type.ExperienceSource;
import net.Indyuce.mmocore.api.quest.objective.Objective;
import net.Indyuce.mmocore.api.quest.trigger.Trigger;

public interface MMOLoader {
	public Condition loadCondition(MMOLineConfig config);

	public Trigger loadTrigger(MMOLineConfig config);
	
	public DropItem loadDropItem(MMOLineConfig config);
	
	public Objective loadObjective(MMOLineConfig config, ConfigurationSection section);
	
	public ExperienceSource<?> loadExperienceSource(MMOLineConfig config, Profession profession);
}
