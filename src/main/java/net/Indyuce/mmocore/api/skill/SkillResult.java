package net.Indyuce.mmocore.api.skill;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.skill.Skill.SkillInfo;

public class SkillResult {
	private final SkillInfo skill;
	private final int level;
	private final double mana, cooldown;

	private CancelReason cancelReason;

	public SkillResult(PlayerData data, SkillInfo skill) {
		this.skill = skill;

		level = data.getSkillLevel(skill.getSkill());
		cooldown = (skill.getSkill().hasModifier("cooldown") ? data.getSkillData().getCooldown(skill) : 0);
		mana = (skill.getSkill().hasModifier("mana") ? skill.getModifier("mana", level) : 0);
		cancelReason = !data.hasSkillUnlocked(skill.getSkill()) ? CancelReason.LOCKED
				: cooldown > 0 ? CancelReason.COOLDOWN : mana > data.getMana() ? CancelReason.MANA : null;
	}

	public SkillResult(PlayerData data, SkillInfo skill, CancelReason reason) {
		this.skill = skill;
		this.cancelReason = reason;

		level = data.getSkillLevel(skill.getSkill());
		cooldown = skill.getSkill().hasModifier("cooldown") ? data.getSkillData().getCooldown(skill) : 0;
		mana = skill.getSkill().hasModifier("mana") ? skill.getModifier("mana", level) : 0;
	}

	public Skill getSkill() {
		return skill.getSkill();
	}

	public SkillInfo getInfo() {
		return skill;
	}

	public int getLevel() {
		return level;
	}

	public double getManaCost() {
		return mana;
	}

	public double getCooldown() {
		return cooldown;
	}

	public boolean isSuccessful() {
		return cancelReason == null;
	}

	public CancelReason getCancelReason() {
		return cancelReason;
	}

	public void abort() {
		abort(CancelReason.OTHER);
	}

	public void abort(CancelReason reason) {
		cancelReason = reason;
	}

	public double getModifier(String modifier) {
		return skill.getModifier(modifier, level);
	}

	public enum CancelReason {

		// not enough mana
		MANA,

		// skill still on cooldown
		COOLDOWN,

		// skill still not unlocked
		LOCKED,

		// no reason specified
		OTHER;
	}
}
