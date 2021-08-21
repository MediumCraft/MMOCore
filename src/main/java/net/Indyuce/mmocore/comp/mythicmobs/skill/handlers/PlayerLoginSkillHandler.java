package net.Indyuce.mmocore.comp.mythicmobs.skill.handlers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerLoginEvent;

import io.lumine.mythic.utils.Schedulers;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.comp.mythicmobs.skill.MythicSkill;
import net.Indyuce.mmocore.comp.mythicmobs.skill.PassiveMythicSkillHandler;

public class PlayerLoginSkillHandler extends PassiveMythicSkillHandler {
    /**
     * Used when a player logins
     *
     * @param skill
     */
    public PlayerLoginSkillHandler(MythicSkill skill) {
        super(skill);
    }

    @EventHandler
    private void event(PlayerLoginEvent e){
        Schedulers.sync().runLater(() -> {
            castSkill(PlayerData.get( e.getPlayer()));
        }, 50);

    }
}
