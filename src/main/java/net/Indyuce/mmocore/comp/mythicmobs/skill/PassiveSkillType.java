package net.Indyuce.mmocore.comp.mythicmobs.skill;

import org.bukkit.event.Listener;

public enum PassiveSkillType {
	PLAYER_ATTACK,
	PLAYER_DAMAGE;

	public Listener getListener(MythicMobSkill skill) {
		if (this == PLAYER_ATTACK)
			return new PlayerAttackSkillHandler(skill);
		if (this == PLAYER_DAMAGE)
			return new PlayerDamageSkillHandler(skill);
		throw new NullPointerException();
	}
}
