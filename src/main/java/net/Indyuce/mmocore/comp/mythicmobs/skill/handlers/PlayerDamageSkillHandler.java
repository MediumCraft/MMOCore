package net.Indyuce.mmocore.comp.mythicmobs.skill.handlers;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.comp.mythicmobs.skill.MythicSkill;
import net.Indyuce.mmocore.comp.mythicmobs.skill.PassiveSkillHandler;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * Used to handle passive skills which trigger when a player takes damage
 */
public class PlayerDamageSkillHandler extends PassiveSkillHandler {
    public PlayerDamageSkillHandler(MythicSkill skill) {
        super(skill);
    }

    @EventHandler
    private void event(EntityDamageEvent event) {
        if (event.getEntityType() == EntityType.PLAYER && PlayerData.has(event.getEntity().getUniqueId()))
            castSkill(PlayerData.get((Player) event.getEntity()));
    }
}
