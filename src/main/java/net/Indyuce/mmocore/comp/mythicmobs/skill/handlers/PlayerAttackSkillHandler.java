package net.Indyuce.mmocore.comp.mythicmobs.skill.handlers;

import org.bukkit.event.EventHandler;

import net.Indyuce.mmocore.comp.mythicmobs.skill.MythicMobSkill;
import net.Indyuce.mmocore.comp.mythicmobs.skill.PassiveMythicMobSkillHandler;
import net.mmogroup.mmolib.api.event.PlayerAttackEvent;

public class PlayerAttackSkillHandler extends PassiveMythicMobSkillHandler {
	/**
	 * Used to handle passive skills which trigger when a player attacks another
	 * entity
	 */
	public PlayerAttackSkillHandler(MythicMobSkill skill) {
		super(skill);
	}

	@EventHandler
	private void event(PlayerAttackEvent e) {
		castSkill(e.getData().getMMOCore(), e.getEntity());
	}
}