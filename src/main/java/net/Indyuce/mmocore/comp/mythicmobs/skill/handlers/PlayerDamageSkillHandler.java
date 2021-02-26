package net.Indyuce.mmocore.comp.mythicmobs.skill.handlers;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.comp.mythicmobs.skill.MythicMobSkill;
import net.Indyuce.mmocore.comp.mythicmobs.skill.PassiveMythicMobSkillHandler;

public class PlayerDamageSkillHandler extends PassiveMythicMobSkillHandler {
	/**
	 * Used to handle passive skills which trigger when a player takes damage
	 */
	public PlayerDamageSkillHandler(MythicMobSkill skill) {
		super(skill);
	}

	@EventHandler
	private void event(EntityDamageEvent e) {
		if (e.getEntityType() == EntityType.PLAYER && !e.getEntity().hasMetadata("NPC"))
			castSkill(PlayerData.get((Player) e.getEntity()));
	}	
}
