package net.Indyuce.mmocore.comp.mythicmobs.skill.handlers;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import net.Indyuce.mmocore.comp.mythicmobs.skill.MythicMobSkill;
import net.Indyuce.mmocore.comp.mythicmobs.skill.PassiveMythicMobSkillHandler;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class PlayerDamageByEntitySkillHandler extends PassiveMythicMobSkillHandler {
	/**
	 * Used to handle passive skills which trigger when a player takes damage
	 * from another entity
	 */
	public PlayerDamageByEntitySkillHandler(MythicMobSkill skill) {
		super(skill);
	}

	@EventHandler
	private void a(EntityDamageByEntityEvent event) {
		if (event.getEntity().getType() == EntityType.PLAYER && MMOCoreUtils.canTarget(PlayerData.get(event.getEntity().getUniqueId()), event.getDamager()))
			castSkill(PlayerData.get((Player) event.getEntity()), event.getDamager());
	}	
}
