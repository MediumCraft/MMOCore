package net.Indyuce.mmocore.comp.region.pvpmode;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.protection.events.DisallowedPVPEvent;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.comp.flags.CustomFlag;
import io.lumine.mythic.lib.comp.interaction.InteractionType;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.apache.commons.lang.Validate;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PvPModeListener implements Listener {
    public PvPModeListener() {
        Validate.isTrue(WorldGuard.getInstance().getPlatform().getSessionManager().registerHandler(PvPModeHandler.FACTORY, null), "Could not register WG handler for PvP mode");
    }

    @EventHandler(ignoreCancelled = true)
    public void unblockPvp(DisallowedPVPEvent event) {
        PlayerData defender;
        if (!PlayerData.get(event.getAttacker()).getCombat().isInPvpMode() || !(defender = PlayerData.get(event.getDefender())).getCombat().isInPvpMode())
            return;

        if (!defender.getCombat().canQuitPvpMode() ||
                (!defender.getCombat().isInvulnerable() && MythicLib.plugin.getFlags().isFlagAllowed(event.getDefender().getLocation(), CustomFlag.PVP_MODE)))
            if (MythicLib.plugin.getEntities().canInteract(event.getAttacker(), event.getDefender(), InteractionType.OFFENSE_ACTION, true))
                event.setCancelled(true);
    }
}
