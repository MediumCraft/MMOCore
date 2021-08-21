package net.Indyuce.mmocore.comp.mythicmobs.skill.handlers;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityShootBowEvent;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.comp.mythicmobs.skill.MythicSkill;
import net.Indyuce.mmocore.comp.mythicmobs.skill.PassiveMythicSkillHandler;

public class ShootBowSkillHandler extends PassiveMythicSkillHandler {
	/**
	 * Used to handle passive skills which trigger when a player shoots a bow
	 */
	public ShootBowSkillHandler(MythicSkill skill) {
		super(skill);
	}

	@EventHandler
	private void event(EntityShootBowEvent e) {
		if(e.getEntity().getType() == EntityType.PLAYER)
			castSkill(PlayerData.get((Player) e.getEntity()), e.getProjectile());
	}
}
