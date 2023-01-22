package net.Indyuce.mmocore.comp.region.pvpmode;

import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.FlagValueChangeHandler;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.comp.flags.CustomFlag;
import io.lumine.mythic.lib.comp.flags.WorldGuardFlags;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class PvPModeHandler extends FlagValueChangeHandler<State> {

    @NotNull
    private PlayerData playerData;

    private long lastMessage;

    private static final long MESSAGE_TIMEOUT = 3 * 1000;

    public static final Factory FACTORY = new Factory() {
        public final WorldGuardFlags wgFlags = Objects.requireNonNull(MythicLib.plugin.getFlags().getHandler(WorldGuardFlags.class), "Could not reach ML compatibility class for WG");

        @Override
        public PvPModeHandler create(Session session) {
            return new PvPModeHandler(session, wgFlags.toWorldGuard(CustomFlag.PVP_MODE));
        }
    };

    public PvPModeHandler(Session session, StateFlag flag) {
        super(session, flag);
    }

    /**
     * Triggered when WorldGuard initializes the value for the first time,
     * on player login or world change for instance.
     */
    @Override
    protected void onInitialValue(LocalPlayer player, ApplicableRegionSet set, State value) {
        try {
            playerData = PlayerData.get(player.getUniqueId());
        } catch (Exception exception) {
            // Citizens.
        }
    }

    /**
     * Triggered when WorldGuard does not find a region setting the value of the flag.
     * In that case, put PvP mode to its default setting that is OFF.
     */
    @Override
    protected boolean onAbsentValue(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, State lastValue, MoveType moveType) {
        return true;
    }

    /**
     * Triggered when a player changes region and finds a new value for that flag.
     * In that case, apply the new setting and display messages if needed.
     */
    @Override
    protected boolean onSetValue(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, State currentValue, State lastValue, MoveType moveType) {

        // Do nothing if pvpmode is disabled.
        if (isInvalid() || !playerData.getCombat().isInPvpMode())
            return true;

        final boolean newPvpMode = toBoolean(currentValue);
        final boolean lastPvpMode = toBoolean(lastValue);

        if (!playerData.getCombat().canQuitPvpMode()) {

            // Leaving a custom Pvp zone
            if (!newPvpMode && lastPvpMode && canSendMessage()) {
                lastMessage = System.currentTimeMillis();
                final double remaining = (playerData.getCombat().getLastHit() + MMOCore.plugin.configManager.pvpModeCombatTimeout * 1000 - System.currentTimeMillis()) / 1000;
                MMOCore.plugin.configManager.getSimpleMessage("pvp-mode.leave", "remaining", MythicLib.plugin.getMMOConfig().decimal.format(remaining)).send(playerData.getPlayer());
            }

        } else if (newPvpMode && !lastPvpMode) {

            // Apply invulnerability
            playerData.getCombat().applyInvulnerability();

            // Entering Pvp zone
            if (canSendMessage()) {
                lastMessage = System.currentTimeMillis();
                MMOCore.plugin.configManager.getSimpleMessage("pvp-mode.enter", "time", MythicLib.plugin.getMMOConfig().decimal.format(MMOCore.plugin.configManager.pvpModeInvulnerability)).send(playerData.getPlayer());
            }
        }

        return true;
    }

    private boolean isInvalid() {
        return playerData == null;
    }

    private boolean toBoolean(@Nullable State state) {
        return state == State.ALLOW;
    }

    private boolean canSendMessage() {
        return System.currentTimeMillis() > lastMessage + MESSAGE_TIMEOUT;
    }
}
