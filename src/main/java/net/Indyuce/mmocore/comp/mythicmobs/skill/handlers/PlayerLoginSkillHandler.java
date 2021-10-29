package net.Indyuce.mmocore.comp.mythicmobs.skill.handlers;

import io.lumine.mythic.utils.Schedulers;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.comp.mythicmobs.skill.MythicSkill;
import net.Indyuce.mmocore.comp.mythicmobs.skill.PassiveSkillHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerLoginEvent;

/**
 * Used when a player logins
 */
public class PlayerLoginSkillHandler extends PassiveSkillHandler {
    public PlayerLoginSkillHandler(MythicSkill skill) {
        super(skill);
    }

    @EventHandler
    private void event(PlayerLoginEvent event) {
        Schedulers.sync().runLater(() -> castSkill(PlayerData.get(event.getPlayer())), 50);
    }
}
