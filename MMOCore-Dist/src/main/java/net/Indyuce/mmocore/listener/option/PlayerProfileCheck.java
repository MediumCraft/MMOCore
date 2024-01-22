package net.Indyuce.mmocore.listener.option;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.comp.interaction.InteractionType;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.manager.InventoryManager;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class PlayerProfileCheck implements Listener {

    @EventHandler
    public void a(PlayerInteractEntityEvent event) {
        if (event.getRightClicked().getType() != EntityType.PLAYER || !event.getPlayer().isSneaking() || !MythicLib.plugin.getEntities().canTarget(event.getPlayer(), event.getRightClicked(), InteractionType.SUPPORT_ACTION))
            return;

        InventoryManager.PLAYER_STATS.newInventory(PlayerData.get((Player) event.getRightClicked()), PlayerData.get(event.getPlayer())).open();
    }
}
