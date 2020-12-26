package net.Indyuce.mmocore.api.skill.result;

import org.bukkit.entity.LivingEntity;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.skill.Skill.SkillInfo;
import net.Indyuce.mmocore.api.skill.SkillResult;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.MMORayTraceResult;

public class TargetSkillResult extends SkillResult {
	private LivingEntity target;

	/**
	 * @param data  Player casting the skill
	 * @param skill Skill being cast
	 * @param range Skill raycast range
	 */
	public TargetSkillResult(PlayerData data, SkillInfo skill, double range) {
		this(data, skill, range, false);
	}

	/**
	 * @param data  Player casting the skill
	 * @param skill Skill being cast
	 * @param range Skill raycast range
	 * @param buff  If the skill is a buff ie if it can be cast on party members
	 */
	public TargetSkillResult(PlayerData data, SkillInfo skill, double range, boolean buff) {
		super(data, skill);

		if (isSuccessful()) {
			MMORayTraceResult result = MMOLib.plugin.getVersion().getWrapper().rayTrace(data.getPlayer(), range,
					entity -> MMOCoreUtils.canTarget(data, entity, buff));
			if (!result.hasHit())
				abort();
			else
				target = result.getHit();
		}
	}

	// check skill result abort reason instead
	@Deprecated
	public boolean hasTarget() {
		return target != null;
	}

	public LivingEntity getTarget() {
		return target;
	}
}
