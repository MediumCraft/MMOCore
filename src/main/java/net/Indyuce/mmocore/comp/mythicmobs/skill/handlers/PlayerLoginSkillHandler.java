package net.Indyuce.mmocore.comp.mythicmobs.skill.handlers;

import net.Indyuce.mmocore.api.event.PlayerDataLoadEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.comp.mythicmobs.skill.MythicMobSkill;
import net.Indyuce.mmocore.comp.mythicmobs.skill.PassiveMythicMobSkillHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

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
    private void event(PlayerDataLoadEvent e){
            castSkill(PlayerData.get((Player) e));
    }
}
