package net.Indyuce.mmocore.api.skill;

import org.bukkit.entity.LivingEntity;
import org.bukkit.util.RayTraceResult;

import net.Indyuce.mmocore.MMOCoreUtils;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.skill.Skill.SkillInfo;

public class TargetSkillResult extends SkillResult {
	private LivingEntity target;

	public TargetSkillResult(PlayerData data, SkillInfo skill, double range) {
		super(data, skill);

		if (isSuccessful()) {
			RayTraceResult result = data.getPlayer().getWorld().rayTraceEntities(data.getPlayer().getEyeLocation(), data.getPlayer().getEyeLocation().getDirection(), range, (entity) -> MMOCoreUtils.canTarget(data.getPlayer(), entity));
			if (result == null)
				abort(CancelReason.OTHER);
			else
				target = (LivingEntity) result.getHitEntity();
		}
	}

	public LivingEntity getTarget() {
		return target;
	}
}
