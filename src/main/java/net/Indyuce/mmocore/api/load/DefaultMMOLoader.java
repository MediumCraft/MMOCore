package net.Indyuce.mmocore.api.load;

import org.bukkit.configuration.ConfigurationSection;

import net.Indyuce.mmocore.api.droptable.condition.Condition;
import net.Indyuce.mmocore.api.droptable.condition.WorldCondition;
import net.Indyuce.mmocore.api.droptable.dropitem.DropItem;
import net.Indyuce.mmocore.api.droptable.dropitem.DropTableDropItem;
import net.Indyuce.mmocore.api.droptable.dropitem.GoldDropItem;
import net.Indyuce.mmocore.api.droptable.dropitem.NoteDropItem;
import net.Indyuce.mmocore.api.droptable.dropitem.VanillaDropItem;
import net.Indyuce.mmocore.api.experience.Profession;
import net.Indyuce.mmocore.api.experience.source.BrewPotionExperienceSource;
import net.Indyuce.mmocore.api.experience.source.EnchantItemExperienceSource;
import net.Indyuce.mmocore.api.experience.source.FishItemExperienceSource;
import net.Indyuce.mmocore.api.experience.source.KillMobExperienceSource;
import net.Indyuce.mmocore.api.experience.source.MineBlockExperienceSource;
import net.Indyuce.mmocore.api.experience.source.RepairItemExperienceSource;
import net.Indyuce.mmocore.api.experience.source.SmeltItemExperienceSource;
import net.Indyuce.mmocore.api.experience.source.type.ExperienceSource;
import net.Indyuce.mmocore.api.quest.objective.ClickonObjective;
import net.Indyuce.mmocore.api.quest.objective.GoToObjective;
import net.Indyuce.mmocore.api.quest.objective.KillMobObjective;
import net.Indyuce.mmocore.api.quest.objective.MineBlockObjective;
import net.Indyuce.mmocore.api.quest.objective.Objective;
import net.Indyuce.mmocore.api.quest.trigger.CommandTrigger;
import net.Indyuce.mmocore.api.quest.trigger.ExperienceTrigger;
import net.Indyuce.mmocore.api.quest.trigger.ItemTrigger;
import net.Indyuce.mmocore.api.quest.trigger.ManaTrigger;
import net.Indyuce.mmocore.api.quest.trigger.MessageTrigger;
import net.Indyuce.mmocore.api.quest.trigger.SoundTrigger;
import net.Indyuce.mmocore.api.quest.trigger.StelliumTrigger;
import net.Indyuce.mmocore.api.quest.trigger.Trigger;

public class DefaultMMOLoader implements MMOLoader {

	@Override
	public Trigger loadTrigger(MMOLineConfig config) {
		if (config.getKey().equals("message"))
			return new MessageTrigger(config);

		if (config.getKey().equals("sound") || config.getKey().equals("playsound"))
			return new SoundTrigger(config);

		if (config.getKey().equals("mana"))
			return new ManaTrigger(config);

		if (config.getKey().equals("stellium"))
			return new StelliumTrigger(config);

		if (config.getKey().equals("command"))
			return new CommandTrigger(config);

		if (config.getKey().equals("item") || config.getKey().equals("vanilla"))
			return new ItemTrigger(config);

		if (config.getKey().equals("exp") || config.getKey().equals("experience"))
			return new ExperienceTrigger(config);

		return null;
	}

	@Override
	public DropItem loadDropItem(MMOLineConfig config) {
		if (config.getKey().equals("droptable"))
			return new DropTableDropItem(config);

		if (config.getKey().equals("vanilla"))
			return new VanillaDropItem(config);

		if (config.getKey().equals("note"))
			return new NoteDropItem(config);

		if (config.getKey().equals("gold") || config.getKey().equals("coin"))
			return new GoldDropItem(config);

		return null;
	}

	@Override
	public Objective loadObjective(MMOLineConfig config, ConfigurationSection section) {
		if (config.getKey().equals("goto"))
			return new GoToObjective(section, config);

		if (config.getKey().equals("mineblock"))
			return new MineBlockObjective(section, config);

		if (config.getKey().equals("killmob"))
			return new KillMobObjective(section, config);

		if (config.getKey().equals("clickon"))
			return new ClickonObjective(section, config);

		return null;
	}

	@Override
	public Condition loadCondition(MMOLineConfig config) {
		if (config.getKey().equals("world"))
			return new WorldCondition(config);

		return null;
	}

	@Override
	public ExperienceSource<?> loadExperienceSource(MMOLineConfig config, Profession profession) {
		if (config.getKey().equals("fishitem"))
			return new FishItemExperienceSource(profession, config);

		if (config.getKey().equals("killmob"))
			return new KillMobExperienceSource(profession, config);

		if (config.getKey().equals("mineblock"))
			return new MineBlockExperienceSource(profession, config);

		if (config.getKey().equals("brewpotion"))
			return new BrewPotionExperienceSource(profession, config);

		if (config.getKey().equals("smeltitem"))
			return new SmeltItemExperienceSource(profession, config);

		if (config.getKey().equals("enchantitem"))
			return new EnchantItemExperienceSource(profession, config);

		if (config.getKey().equals("repairitem"))
			return new RepairItemExperienceSource(profession, config);

		return null;
	}
}
