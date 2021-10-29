package net.Indyuce.mmocore.comp.mythicmobs.skill.handlers;

import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.comp.mythicmobs.skill.MythicSkill;
import net.Indyuce.mmocore.comp.mythicmobs.skill.PassiveSkillHandler;
import org.bukkit.event.EventHandler;

/**
 * Used to handle passive skills which trigger when a player attacks another
 * entity
 */
public class PlayerAttackSkillHandler extends PassiveSkillHandler {
    public PlayerAttackSkillHandler(MythicSkill skill) {
        super(skill);
    }

    @EventHandler
    private void event(PlayerAttackEvent event) {
        castSkill(PlayerData.get(event.getData().getUniqueId()), event.getEntity());
    }
}