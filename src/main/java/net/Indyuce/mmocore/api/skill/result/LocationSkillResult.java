package net.Indyuce.mmocore.api.skill.result;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.util.RayTraceResult;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.skill.Skill.SkillInfo;
import net.Indyuce.mmocore.api.skill.SkillResult;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;

public class LocationSkillResult extends SkillResult {
	private Location loc;

	/**
	 * @param data  Player casting the skill
	 * @param skill Skill being cast
	 * @param range Skill raycast range
	 */
	public LocationSkillResult(PlayerData data, SkillInfo skill, double range) {
		this(data, skill, range, false);
	}

	/**
	 * @param data  Player casting the skill
	 * @param skill Skill being cast
	 * @param range Skill raycast range
	 * @param buff  If the skill is a buff ie if it can be cast on party members
	 */
	public LocationSkillResult(PlayerData data, SkillInfo skill, double range, boolean buff) {
		super(data, skill);

		if (isSuccessful()) {

			RayTraceResult result = data.getPlayer().getWorld().rayTrace(data.getPlayer().getEyeLocation(),
					data.getPlayer().getEyeLocation().getDirection(), range, FluidCollisionMode.ALWAYS, true, 1.0D,
					entity -> MMOCoreUtils.canTarget(data, entity, buff));
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
