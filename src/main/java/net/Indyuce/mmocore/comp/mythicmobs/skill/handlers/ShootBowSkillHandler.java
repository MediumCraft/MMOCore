package net.Indyuce.mmocore.comp.mythicmobs.skill.handlers;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.comp.mythicmobs.skill.MythicSkill;
import net.Indyuce.mmocore.comp.mythicmobs.skill.PassiveSkillHandler;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityShootBowEvent;

/**
 * Used to handle passive skills which trigger when a player shoots a bow
 */
public class ShootBowSkillHandler extends PassiveSkillHandler {
    public ShootBowSkillHandler(MythicSkill skill) {
        super(skill);
    }

    @EventHandler
    private void event(EntityShootBowEvent event) {
        if (event.getEntity().getType() == EntityType.PLAYER && PlayerData.has(event.getEntity().getUniqueId()))
            castSkill(PlayerData.get((Player) event.getEntity()), event.getProjectile());
    }
}
