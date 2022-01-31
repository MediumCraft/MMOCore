package net.Indyuce.mmocore.player.playerclass;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.skill.custom.mechanic.Mechanic;
import io.lumine.mythic.lib.skill.result.SkillResult;
import io.lumine.mythic.lib.skill.trigger.TriggerMetadata;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.quest.trigger.Trigger;
import net.Indyuce.mmocore.listener.ClassTriggers;
import org.apache.commons.lang.Validate;

import java.util.ArrayList;
import java.util.List;

/**
 * Class triggers are shortcuts for the MythicLib custom
 * passive skill creation system.
 * <p>
 * A class trigger is defined by a {@link ClassTriggerType} which
 * determines when the action is performed, and a set of {@link Mechanic}
 * which determines what happens when triggered.
 * <p>
 * Class triggers fully replace 'event triggers' which were an
 * over simplified implementation of passive skills.
 *
 * @author jules
 * @see {@link ClassTriggers}
 */
public class ClassTrigger {
    private final ClassTriggerType type;
    private final List<Trigger> triggers = new ArrayList<>();
    // private final CustomSkill skill = new CustomSkill("classTrigger", false);
    // private final Skill castableSkill = new SimpleSkill(new MythicLibSkillHandler(skill));

    public ClassTrigger(String triggerTypeString, List<String> mechanicStringList) {
        Validate.notNull(mechanicStringList, "Mechanic list cannot be null");
        type = ClassTriggerType.valueOf(UtilityMethods.enumName(triggerTypeString));


        for (String key : mechanicStringList) {
            MMOLineConfig config = new MMOLineConfig(key);
            triggers.add(MMOCore.plugin.loadManager.loadTrigger(config));
            //     ConfigObject config = new LineConfigObject(new MMOLineConfig(key));
            //     Mechanic mechanic = MythicLib.plugin.getSkills().loadMechanic(config);
            //     skill.getMechanics().add(mechanic);
        }
    }

    public ClassTriggerType getType() {
        return type;
    }

    @Deprecated
    public SkillResult trigger(TriggerMetadata triggerMeta) {
        // return castableSkill.cast(triggerMeta);
        return null;
    }

    public void trigger(PlayerData player) {
        triggers.forEach(trigger -> trigger.apply(player));
    }
}
