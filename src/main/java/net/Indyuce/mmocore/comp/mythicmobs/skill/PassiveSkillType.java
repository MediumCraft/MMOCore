package net.Indyuce.mmocore.comp.mythicmobs.skill;

import net.Indyuce.mmocore.comp.mythicmobs.skill.handlers.EntityDeathSkillHandler;
import net.Indyuce.mmocore.comp.mythicmobs.skill.handlers.PlayerAttackSkillHandler;
import net.Indyuce.mmocore.comp.mythicmobs.skill.handlers.PlayerDamageByEntitySkillHandler;
import net.Indyuce.mmocore.comp.mythicmobs.skill.handlers.PlayerDamageSkillHandler;
import net.Indyuce.mmocore.comp.mythicmobs.skill.handlers.PlayerDeathSkillHandler;
import net.Indyuce.mmocore.comp.mythicmobs.skill.handlers.ShootBowSkillHandler;

public enum PassiveSkillType {
	PLAYER_ATTACK,
	PLAYER_DAMAGE,
	PLAYER_DAMAGE_BY_ENTITY,
	PLAYER_DEATH,
	PLAYER_KILL_ENTITY,
	SHOOT_BOW;

	public PassiveMythicMobSkillHandler getHandler(MythicMobSkill skill) {
		if (this == PLAYER_ATTACK)
			return new PlayerAttackSkillHandler(skill);
		if (this == PLAYER_DAMAGE)
			return new PlayerDamageSkillHandler(skill);
		if (this == PLAYER_KILL_ENTITY)
			return new EntityDeathSkillHandler(skill);
		if (this == PLAYER_DAMAGE_BY_ENTITY)
			return new PlayerDamageByEntitySkillHandler(skill);
		if (this == PLAYER_DEATH)
			return new PlayerDeathSkillHandler(skill);
		if (this == SHOOT_BOW)
			return new ShootBowSkillHandler(skill);
		throw new NullPointerException();
	}
}
