package net.Indyuce.mmocore.listener;

import io.lumine.mythic.lib.api.player.MMOPlayerData;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.event.PlayerChangeClassEvent;
import net.Indyuce.mmocore.api.event.PlayerLevelUpEvent;
import net.Indyuce.mmocore.script.trigger.MMOCoreTriggerType;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * This class calls trigger types registered by MMOCore
 * which are specific to player classes.
 *
 * @see {@link MMOCoreTriggerType}
 */
public class ClassScriptListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onClassChange(PlayerChangeClassEvent event) {

        // With delay
        Bukkit.getScheduler().runTask(MMOCore.plugin, () -> {
            final MMOPlayerData caster = event.getData().getMMOPlayerData();
            caster.triggerSkills(MMOCoreTriggerType.CLASS_CHOSEN, null);
        });
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onLevelUp(PlayerLevelUpEvent event) {

        // With delay
        Bukkit.getScheduler().runTask(MMOCore.plugin, () -> {
            final MMOPlayerData caster = event.getData().getMMOPlayerData();
            caster.triggerSkills(MMOCoreTriggerType.LEVEL_UP, null);
        });
    }

   /* @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {
        performScripts(event.getPlayer(), MMOCoreTriggerType.BREAK_BLOCK);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockBreakEvent event) {
        performScripts(event.getPlayer(), MMOCoreTriggerType.PLACE_BLOCK);
    }*/
}
