package net.Indyuce.mmocore.listener;

import io.lumine.mythic.lib.api.event.unlocking.ItemLockedEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class SkillLockingListener implements Listener {

    /**
     * Unbind the skill from boundSkills when it becomes locked.
     */
    @EventHandler
    public void onSkillLock(ItemLockedEvent event) {
        if (event.getItemTypeId().equals("skill")) {
            PlayerData playerData = PlayerData.get(event.getData().getUniqueId());
            playerData.mapBoundSkills()
                    .forEach((slot, skillId) -> {
                        if (skillId.equalsIgnoreCase(event.getItemId()))
                            playerData.unbindSkill(slot);
                    });
        }
    }
}
