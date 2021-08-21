package net.Indyuce.mmocore.comp.mythicmobs.skill.handlers;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.comp.mythicmobs.skill.MythicSkill;
import net.Indyuce.mmocore.comp.mythicmobs.skill.PassiveMythicSkillHandler;

public class PlayerDeathSkillHandler extends PassiveMythicSkillHandler {
	/**
	 * Used to handle passive skills which trigger when a player dies
	 */
	public PlayerDeathSkillHandler(MythicSkill skill) {
		super(skill);
	}

	@EventHandler
	private void event(EntityDeathEvent e) {
		if (e.getEntityType() == EntityType.PLAYER)
			castSkill(PlayerData.get((Player) e.getEntity()));
	}	
}
