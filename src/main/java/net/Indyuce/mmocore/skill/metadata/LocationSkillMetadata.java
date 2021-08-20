package net.Indyuce.mmocore.skill.metadata;

import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import net.Indyuce.mmocore.skill.CasterMetadata;
import net.Indyuce.mmocore.skill.Skill.SkillInfo;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.util.RayTraceResult;

public class LocationSkillMetadata extends SkillMetadata {
    private Location loc;

    /**
     * @param caster Player casting the skill
     * @param skill  Skill being cast
     * @param range  Skill raycast range
     */
    public LocationSkillMetadata(CasterMetadata caster, SkillInfo skill, double range) {
        this(caster, skill, range, false);
    }

    /**
     * @param caster Player casting the skill
     * @param skill  Skill being cast
     * @param range  Skill raycast range
     * @param buff   If the skill is a buff ie if it can be cast on party members
     */
    public LocationSkillMetadata(CasterMetadata caster, SkillInfo skill, double range, boolean buff) {
        super(caster, skill);

        if (isSuccessful()) {

            RayTraceResult result = caster.getPlayer().getWorld().rayTrace(caster.getPlayer().getEyeLocation(),
                    caster.getPlayer().getEyeLocation().getDirection(), range, FluidCollisionMode.ALWAYS, true, 1.0D,
                    entity -> MMOCoreUtils.canTarget(caster.getPlayerData(), entity, buff));
            if (result == null)
                abort(CancelReason.OTHER);
            else
                loc = result.getHitBlock() != null ? result.getHitBlock().getLocation()
                        : result.getHitEntity() != null ? result.getHitEntity().getLocation() : null;
        }
    }

    public boolean hasHit() {
        return loc != null;
    }

    public Location getHit() {
        return loc;
    }
}
