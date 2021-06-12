package net.Indyuce.mmocore.comp.mythicmobs.skill.handlers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerLoginEvent;

import io.lumine.utils.Schedulers;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.comp.mythicmobs.skill.MythicMobSkill;
import net.Indyuce.mmocore.comp.mythicmobs.skill.PassiveMythicMobSkillHandler;

public class PlayerLoginSkillHandler extends PassiveMythicMobSkillHandler {
    /**
     * Used when a player logins
     *
     * @param skill
     */
    public PlayerLoginSkillHandler(MythicMobSkill skill) {
        super(skill);
    }

    @EventHandler
    private void event(PlayerLoginEvent e){
        Schedulers.sync().runLater(() -> {
            castSkill(PlayerData.get( e.getPlayer()));
        }, 50);

    }
}
