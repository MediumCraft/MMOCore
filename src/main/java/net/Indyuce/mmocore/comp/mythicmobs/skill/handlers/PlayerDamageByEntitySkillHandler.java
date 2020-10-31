package net.Indyuce.mmocore.comp.mythicmobs.skill.handlers;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.comp.mythicmobs.skill.MythicMobSkill;
import net.Indyuce.mmocore.comp.mythicmobs.skill.PassiveMythicMobSkillHandler;

public class PlayerDamageByEntitySkillHandler extends PassiveMythicMobSkillHandler {
	/**
	 * Used to handle passive skills which trigger when a player takes damage
	 * from another entity
	 */
	public PlayerDamageByEntitySkillHandler(MythicMobSkill skill) {
		super(skill);
	}

	@EventHandler
	private void event(EntityDamageByEntityEvent e) {
		if (e.getEntity().getType() == EntityType.PLAYER)
			castSkill(PlayerData.get((Player) e.getEntity()), e.getDamager());
	}	
}
