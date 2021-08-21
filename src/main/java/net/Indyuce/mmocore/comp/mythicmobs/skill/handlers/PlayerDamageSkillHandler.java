package net.Indyuce.mmocore.comp.mythicmobs.skill.handlers;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.comp.mythicmobs.skill.MythicSkill;
import net.Indyuce.mmocore.comp.mythicmobs.skill.PassiveMythicSkillHandler;

public class PlayerDamageSkillHandler extends PassiveMythicSkillHandler {
	/**
	 * Used to handle passive skills which trigger when a player takes damage
	 */
	public PlayerDamageSkillHandler(MythicSkill skill) {
		super(skill);
	}

	@EventHandler
	private void event(EntityDamageEvent event) {
		if (event.getEntityType() == EntityType.PLAYER && !event.getEntity().hasMetadata("NPC"))
			castSkill(PlayerData.get((Player) event.getEntity()));
	}	
}
