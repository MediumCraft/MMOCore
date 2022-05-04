package net.Indyuce.mmocore.api.load;

import net.Indyuce.mmocore.experience.dispenser.ExperienceDispenser;
import net.Indyuce.mmocore.loot.droptable.condition.*;
import org.bukkit.configuration.ConfigurationSection;

import net.Indyuce.mmocore.api.block.BlockType;
import net.Indyuce.mmocore.api.block.SkullBlockType;
import net.Indyuce.mmocore.api.block.VanillaBlockType;
import net.Indyuce.mmocore.loot.droptable.dropitem.DropItem;
import net.Indyuce.mmocore.loot.droptable.dropitem.DropTableDropItem;
import net.Indyuce.mmocore.loot.droptable.dropitem.GoldDropItem;
import net.Indyuce.mmocore.loot.droptable.dropitem.NoteDropItem;
import net.Indyuce.mmocore.loot.droptable.dropitem.VanillaDropItem;
import net.Indyuce.mmocore.experience.source.BrewPotionExperienceSource;
import net.Indyuce.mmocore.experience.source.CraftItemExperienceSource;
import net.Indyuce.mmocore.experience.source.EnchantItemExperienceSource;
import net.Indyuce.mmocore.experience.source.FishItemExperienceSource;
import net.Indyuce.mmocore.experience.source.KillMobExperienceSource;
import net.Indyuce.mmocore.experience.source.MineBlockExperienceSource;
import net.Indyuce.mmocore.experience.source.PlaceBlockExperienceSource;
import net.Indyuce.mmocore.experience.source.RepairItemExperienceSource;
import net.Indyuce.mmocore.experience.source.SmeltItemExperienceSource;
import net.Indyuce.mmocore.experience.source.type.ExperienceSource;
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
import net.Indyuce.mmocore.api.quest.trigger.StaminaTrigger;
import net.Indyuce.mmocore.api.quest.trigger.StelliumTrigger;
import net.Indyuce.mmocore.api.quest.trigger.Trigger;
import io.lumine.mythic.lib.api.MMOLineConfig;

public class DefaultMMOLoader extends MMOLoader {

	@Override
	public Trigger loadTrigger(MMOLineConfig config) {
		if (config.getKey().equals("message"))
			return new MessageTrigger(config);

		if (config.getKey().equals("sound") || config.getKey().equals("playsound"))
			return new SoundTrigger(config);

		if (config.getKey().equals("mana"))
			return new ManaTrigger(config);

		if (config.getKey().equals("stamina"))
			return new StaminaTrigger(config);
		
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
		if(config.getKey().equals("distance"))
			return new DistanceCondition(config);

		if (config.getKey().equals("world"))
			return new WorldCondition(config);

		if (config.getKey().equals("biome"))
			return new BiomeCondition(config);

		if (config.getKey().equals("level"))
			return new LevelCondition(config);
		
		if (config.getKey().equals("permission"))
			return new PermissionCondition(config);

		return null;
	}

	@Override
	public ExperienceSource<?> loadExperienceSource(MMOLineConfig config, ExperienceDispenser dispenser) {
		if (config.getKey().equals("fishitem"))
			return new FishItemExperienceSource(dispenser, config);

		if (config.getKey().equals("killmob"))
			return new KillMobExperienceSource(dispenser, config);

		if (config.getKey().equals("mineblock"))
			return new MineBlockExperienceSource(dispenser, config);

		if (config.getKey().equals("placeblock"))
			return new PlaceBlockExperienceSource(dispenser, config);

		if (config.getKey().equals("brewpotion"))
			return new BrewPotionExperienceSource(dispenser, config);

		if (config.getKey().equals("smeltitem"))
			return new SmeltItemExperienceSource(dispenser, config);

		if (config.getKey().equals("enchantitem"))
			return new EnchantItemExperienceSource(dispenser, config);

		if (config.getKey().equals("repairitem"))
			return new RepairItemExperienceSource(dispenser, config);

		if (config.getKey().equals("craftitem"))
			return new CraftItemExperienceSource(dispenser, config);

		return null;
	}

	@Override
	public BlockType loadBlockType(MMOLineConfig config) {

		if (config.getKey().equalsIgnoreCase("vanilla"))
			return new VanillaBlockType(config);
		
		if (config.getKey().equalsIgnoreCase("skull") || config.getKey().equals("head") || config.getKey().equals("playerhead"))
			return new SkullBlockType(config);

		return null;
	}
}
