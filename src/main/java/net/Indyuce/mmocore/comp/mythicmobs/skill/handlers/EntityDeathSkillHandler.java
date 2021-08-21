package net.Indyuce.mmocore.comp.mythicmobs.skill.handlers;

import io.lumine.mythic.lib.api.event.EntityKillEntityEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.comp.mythicmobs.skill.MythicSkill;
import net.Indyuce.mmocore.comp.mythicmobs.skill.PassiveMythicSkillHandler;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;


public class EntityDeathSkillHandler extends PassiveMythicSkillHandler {
	/**
	 * Used to handle passive skills which trigger when a player kills
	 * another entity
	 */
	public EntityDeathSkillHandler(MythicSkill skill) {
		super(skill);
	}

	@EventHandler
	private void event(EntityKillEntityEvent e) {
		if (e.getEntity().getType() == EntityType.PLAYER)
			castSkill(PlayerData.get((Player) e.getEntity()), e.getTarget());
	}	
}
