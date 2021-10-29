package net.Indyuce.mmocore.comp.mythicmobs.skill.handlers;

import io.lumine.mythic.lib.api.event.PlayerKillEntityEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.comp.mythicmobs.skill.MythicSkill;
import net.Indyuce.mmocore.comp.mythicmobs.skill.PassiveSkillHandler;
import org.bukkit.event.EventHandler;


/**
 * Used to handle passive skills which trigger when a player kills
 * another entity
 */
public class EntityDeathSkillHandler extends PassiveSkillHandler {
    public EntityDeathSkillHandler(MythicSkill skill) {
        super(skill);
    }

    @EventHandler
    private void event(PlayerKillEntityEvent event) {
        castSkill(PlayerData.get(event.getPlayer()), event.getTarget());
    }
}
