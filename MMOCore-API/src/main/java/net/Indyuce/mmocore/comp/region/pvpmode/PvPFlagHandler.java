package net.Indyuce.mmocore.comp.region.pvpmode;

import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import io.lumine.mythic.lib.MythicLib;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigMessage;

public class PvPFlagHandler extends MMOCoreFlagHandler {

    public static final Factory FACTORY = new Factory() {

        @Override
        public PvPFlagHandler create(Session session) {
            return new PvPFlagHandler(session, Flags.PVP);
        }
    };

    public PvPFlagHandler(Session session, StateFlag flag) {
        super(session, flag);
    }

    @Override
    public State getDefaultState() {
        return State.ALLOW;
    }

    /**
     * Triggered when a player changes region and finds a new value for that flag.
     * In that case, apply the new setting and display messages if needed.
     */
    @Override
    protected boolean onSetValue(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, State currentValue, State lastValue, MoveType moveType) {
        if (isInvalid())
            return true;

        boolean newPvp = toBoolean(currentValue);
        boolean lastPvp = toBoolean(lastValue);

        if (newPvp && !lastPvp) {

            // Apply invulnerability
            playerData.getCombat().setInvulnerable(MMOCore.plugin.configManager.pvpModeInvulnerabilityTimeRegionChange);

            // Send message
            if (canSendMessage()) {
                lastMessage = System.currentTimeMillis();
                ConfigMessage.fromKey("pvp-mode.enter.pvp-mode-on", "time",
                        MythicLib.plugin.getMMOConfig().decimal.format(MMOCore.plugin.configManager.pvpModeInvulnerabilityTimeRegionChange)).send(playerData.getPlayer());
            }
        }
        return true;
    }
}
