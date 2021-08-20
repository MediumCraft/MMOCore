package net.Indyuce.mmocore.skill.metadata;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.MMORayTraceResult;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import net.Indyuce.mmocore.skill.CasterMetadata;
import net.Indyuce.mmocore.skill.Skill.SkillInfo;
import org.bukkit.entity.LivingEntity;

public class TargetSkillMetadata extends SkillMetadata {
    private LivingEntity target;

    /**
     * @param caster Player casting the skill
     * @param skill  Skill being cast
     * @param range  Skill raycast range
     */
    public TargetSkillMetadata(CasterMetadata caster, SkillInfo skill, double range) {
        this(caster, skill, range, false);
    }

    /**
     * @param caster Player casting the skill
     * @param skill  Skill being cast
     * @param range  Skill raycast range
     * @param buff   If the skill is a buff ie if it can be cast on party members
     */
    public TargetSkillMetadata(CasterMetadata caster, SkillInfo skill, double range, boolean buff) {
        super(caster, skill);

        if (isSuccessful()) {
            MMORayTraceResult result = MythicLib.plugin.getVersion().getWrapper().rayTrace(caster.getPlayer(), range, entity -> MMOCoreUtils.canTarget(caster.getPlayerData(), entity, buff));
            if (!result.hasHit())
                abort();
            else
                target = result.getHit();
        }
    }

    public LivingEntity getTarget() {
        return target;
    }
}
