package net.Indyuce.mmocore.comp.mythicmobs.skill.handlers;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.comp.mythicmobs.skill.MythicMobSkill;
import net.Indyuce.mmocore.comp.mythicmobs.skill.PassiveMythicMobSkillHandler;

public class PlayerDeathSkillHandler extends PassiveMythicMobSkillHandler {
	/**
	 * Used to handle passive skills which trigger when a player dies
	 */
	public PlayerDeathSkillHandler(MythicMobSkill skill) {
		super(skill);
	}

	@EventHandler
	private void event(EntityDeathEvent e) {
		if (e.getEntityType() == EntityType.PLAYER)
			castSkill(PlayerData.get((Player) e.getEntity()));
	}	
}
