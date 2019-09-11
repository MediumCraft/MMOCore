package net.Indyuce.mmocore.api.skill;

import org.bukkit.entity.LivingEntity;
import org.bukkit.util.RayTraceResult;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.skill.Skill.SkillInfo;

public class TargetSkillResult extends SkillResult {
	private LivingEntity target;

	public TargetSkillResult(PlayerData data, SkillInfo skill, double range) {
		super(data, skill);

		if (isSuccessful()) {
			RayTraceResult result = MMOCore.plugin.version.getVersionWrapper().rayTraceEntities(data.getPlayer(), data.getPlayer().getEyeLocation().getDirection(), range);
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
