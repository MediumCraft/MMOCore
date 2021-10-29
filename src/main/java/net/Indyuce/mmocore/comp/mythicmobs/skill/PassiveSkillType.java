package net.Indyuce.mmocore.comp.mythicmobs.skill;

import net.Indyuce.mmocore.comp.mythicmobs.skill.handlers.*;

import java.util.function.Function;

public enum PassiveSkillType {

    /**
     * Activates the skill when the player attacks something
     * <p>
     * Trigger target: The entity the player attacked
     */
    PLAYER_ATTACK(skill -> new PlayerAttackSkillHandler(skill)),

    /**
     * Activates the skill when the player takes damage
     * <p>
     * Trigger target: The player
     */
    PLAYER_DAMAGE(skill -> new PlayerDamageSkillHandler(skill)),

    /**
     * Activates the skill when the player takes damage from an entity
     * <p>
     * Trigger target: The entity that damaged the player
     */
    PLAYER_DAMAGE_BY_ENTITY(skill -> new PlayerDamageByEntitySkillHandler(skill)),

    /**
     * Activates the skill when the player dies
     * <p>
     * Trigger target: The player
     */
    PLAYER_DEATH(skill -> new PlayerDeathSkillHandler(skill)),

    /**
     * Activates the skill when a player kills an entity
     * <p>
     * Trigger target:The killed entity
     */
    PLAYER_KILL_ENTITY(skill -> new EntityDeathSkillHandler(skill)),

    /**
     * Activates the skill when a player shoots an arrow from a bow
     * <p>
     * Trigger target: The arrow that was shot
     */
    SHOOT_BOW(skill -> new ShootBowSkillHandler(skill)),

    /**
     * Activates the skill when a player logins
     * <p>
     * Trigger target: The player
     */
    PLAYER_LOGIN(skill -> new PlayerLoginSkillHandler(skill));

    private final Function<MythicSkill, PassiveSkillHandler> handler;

    PassiveSkillType(Function<MythicSkill, PassiveSkillHandler> handler) {
        this.handler = handler;
    }

    public PassiveSkillHandler getHandler(MythicSkill skill) {
        return handler.apply(skill);
    }
}
