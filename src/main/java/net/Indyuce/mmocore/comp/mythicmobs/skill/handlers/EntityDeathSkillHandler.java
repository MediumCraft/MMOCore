package net.Indyuce.mmocore.comp.mythicmobs.skill.handlers;

import io.lumine.mythic.lib.api.event.EntityKillEntityEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.comp.mythicmobs.skill.MythicMobSkill;
import net.Indyuce.mmocore.comp.mythicmobs.skill.PassiveMythicMobSkillHandler;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;


public class EntityDeathSkillHandler extends PassiveMythicMobSkillHandler {
	/**
	 * Used to handle passive skills which trigger when a player kills
	 * another entity
	 */
	public EntityDeathSkillHandler(MythicMobSkill skill) {
		super(skill);
	}

	@EventHandler
	private void event(EntityKillEntityEvent e) {
		if (e.getEntity().getType() == EntityType.PLAYER)
			castSkill(PlayerData.get((Player) e.getEntity()), e.getTarget());
	}	
}
