package net.Indyuce.mmocore.api.skill;

import org.bukkit.entity.LivingEntity;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.skill.Skill.SkillInfo;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.MMORayTraceResult;

public class TargetSkillResult extends SkillResult {
	private LivingEntity target;

	public TargetSkillResult(PlayerData data, SkillInfo skill, double range) {
		super(data, skill);

		if (isSuccessful()) {
			MMORayTraceResult result = MMOLib.plugin.getVersion().getWrapper().rayTrace(data.getPlayer(), range, entity -> MMOCoreUtils.canTarget(data.getPlayer(), entity));
			if (!result.hasHit())
				abort(CancelReason.OTHER);
			else
				target = (LivingEntity) result.getHit();
		}
	}

	public LivingEntity getTarget() {
		return target;
	}
}
