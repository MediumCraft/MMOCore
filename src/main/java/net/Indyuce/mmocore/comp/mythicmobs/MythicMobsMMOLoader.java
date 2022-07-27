package net.Indyuce.mmocore.comp.mythicmobs;

import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.comp.mythicmobs.load.*;
import net.Indyuce.mmocore.api.load.MMOLoader;
import net.Indyuce.mmocore.api.quest.objective.Objective;
import net.Indyuce.mmocore.api.quest.trigger.Trigger;
import net.Indyuce.mmocore.experience.dispenser.ExperienceDispenser;
import net.Indyuce.mmocore.experience.source.type.ExperienceSource;
import net.Indyuce.mmocore.loot.droptable.dropitem.DropItem;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Arrays;
import java.util.List;

public class MythicMobsMMOLoader extends MMOLoader {

    @Override
    public Trigger loadTrigger(MMOLineConfig config) {

        if (config.getKey().equalsIgnoreCase("mmskill") || config.getKey().equalsIgnoreCase("mythicmobskill"))
            return new MythicSkillTrigger(config);

        return null;
    }

    @Override
    public DropItem loadDropItem(MMOLineConfig config) {

        if (config.getKey().equals("mmdroptable"))
            return new MMDropTableDropItem(config);

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
    public List<ExperienceSource<?>> loadExperienceSource(MMOLineConfig config, ExperienceDispenser dispenser) {

        if (config.getKey().equalsIgnoreCase("killmythicmob"))
            return Arrays.asList(new KillMythicMobExperienceSource(dispenser, config));
        if (config.getKey().equalsIgnoreCase("killmythicfaction"))
            return Arrays.asList(new KillMythicFactionExperienceSource(dispenser, config));

        return null;
    }
}
