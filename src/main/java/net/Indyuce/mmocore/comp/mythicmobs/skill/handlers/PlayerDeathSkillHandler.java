package net.Indyuce.mmocore.comp.mythicmobs.skill.handlers;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.comp.mythicmobs.skill.MythicSkill;
import net.Indyuce.mmocore.comp.mythicmobs.skill.PassiveSkillHandler;

/**
 * Used to handle passive skills which trigger when a player dies
 */
public class PlayerDeathSkillHandler extends PassiveSkillHandler {
	public PlayerDeathSkillHandler(MythicSkill skill) {
		super(skill);
	}

	@EventHandler
	private void event(EntityDeathEvent event) {
		if (event.getEntityType() == EntityType.PLAYER && PlayerData.has(event.getEntity().getUniqueId()))
			castSkill(PlayerData.get((Player) event.getEntity()));
	}	
}
