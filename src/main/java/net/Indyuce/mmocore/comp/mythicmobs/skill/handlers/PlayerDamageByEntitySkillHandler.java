package net.Indyuce.mmocore.comp.mythicmobs.skill.handlers;

import io.lumine.mythic.lib.comp.target.InteractionType;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import net.Indyuce.mmocore.comp.mythicmobs.skill.MythicSkill;
import net.Indyuce.mmocore.comp.mythicmobs.skill.PassiveMythicSkillHandler;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class PlayerDamageByEntitySkillHandler extends PassiveMythicSkillHandler {
	/**
	 * Used to handle passive skills which trigger when a player takes damage
	 * from another entity
	 */
	public PlayerDamageByEntitySkillHandler(MythicSkill skill) {
		super(skill);
	}

	@EventHandler
	private void a(EntityDamageByEntityEvent event) {
		if (event.getEntity().getType() == EntityType.PLAYER && MMOCoreUtils.canTarget(PlayerData.get(event.getEntity().getUniqueId()), event.getDamager(), InteractionType.OFFENSE_SKILL))
			castSkill(PlayerData.get((Player) event.getEntity()), event.getDamager());
	}	
}
