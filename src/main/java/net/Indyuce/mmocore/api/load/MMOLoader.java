package net.Indyuce.mmocore.api.load;

import org.bukkit.configuration.ConfigurationSection;

import net.Indyuce.mmocore.api.block.BlockType;
import net.Indyuce.mmocore.api.droptable.condition.Condition;
import net.Indyuce.mmocore.api.droptable.dropitem.DropItem;
import net.Indyuce.mmocore.api.experience.Profession;
import net.Indyuce.mmocore.api.experience.source.type.ExperienceSource;
import net.Indyuce.mmocore.api.quest.objective.Objective;
import net.Indyuce.mmocore.api.quest.trigger.Trigger;
import net.mmogroup.mmolib.api.MMOLineConfig;

public class MMOLoader {

	/*
	 * MMOLoader was initially an interface but it is now a class so devs do not
	 * have to add a new method everytime the class is updated.
	 */
	public Condition loadCondition(MMOLineConfig config) {
		return null;
	}

	public Trigger loadTrigger(MMOLineConfig config) {
		return null;
	}

	public DropItem loadDropItem(MMOLineConfig config) {
		return null;
	}

	public Objective loadObjective(MMOLineConfig config, ConfigurationSection section) {
		return null;
	}

	public ExperienceSource<?> loadExperienceSource(MMOLineConfig config, Profession profession) {
		return null;
	}

	public BlockType loadBlockType(MMOLineConfig config) {
		return null;
	}
}
