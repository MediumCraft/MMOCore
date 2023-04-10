package net.Indyuce.mmocore.api.load;

import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.experience.dispenser.ExperienceDispenser;
import net.Indyuce.mmocore.experience.source.type.ExperienceSource;
import net.Indyuce.mmocore.api.block.BlockType;
import net.Indyuce.mmocore.api.block.SkullBlockType;
import net.Indyuce.mmocore.api.block.VanillaBlockType;
import net.Indyuce.mmocore.api.quest.objective.*;
import net.Indyuce.mmocore.api.quest.trigger.*;
import net.Indyuce.mmocore.experience.source.*;
import net.Indyuce.mmocore.loot.chest.condition.*;
import net.Indyuce.mmocore.loot.droptable.dropitem.*;
import org.bukkit.configuration.ConfigurationSection;

public class DefaultMMOLoader extends MMOLoader {

    @Override
    public Trigger loadTrigger(MMOLineConfig config) {
        if (config.getKey().equals("from"))
            return new FromTrigger(config);

        if (config.getKey().equals("stat"))
            return new StatTrigger(config);

        if(config.getKey().equals("unlock_slot"))
            return new UnlockSlotTrigger(config);

        if (config.getKey().equals("unlock_skill"))
            return new UnlockSkillTrigger(config);

        if (config.getKey().equals("skill_buff"))
            return new SkillModifierTrigger(config);

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
        if (config.getKey().equals("from")) {
            return new FromCondition(config);
        }
        if (config.getKey().equals("distance"))
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
        if (config.getKey().equals("from"))
            return new FromExperienceSource(dispenser, config);

        if (config.getKey().equals("resource"))
            return new ResourceExperienceSource(dispenser, config);

        if (config.getKey().equals("climb"))
            return new ClimbExperienceSource(dispenser, config);

        if (config.getKey().equals("eat"))
            return new EatExperienceSource(dispenser, config);

        if (config.getKey().equals("damagedealt"))
            return new DamageDealtExperienceSource(dispenser, config);

        if (config.getKey().equals("damagetaken"))
            return new DamageTakenExperienceSource(dispenser, config);

        if (config.getKey().equals("move"))
            return new MoveExperienceSource(dispenser, config);

        if (config.getKey().equals("play"))
            return new PlayExperienceSource(dispenser, config);

        if (config.getKey().equals("projectile"))
            return new ProjectileExperienceSource(dispenser, config);

        if (config.getKey().equals("ride"))
            return new RideExperienceSource(dispenser, config);

        if (config.getKey().equals("tame"))
            return new TameExperienceSource(dispenser, config);

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

        if (config.getKey().equals("fishitem"))
            return new FishItemExperienceSource(dispenser, config);

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
