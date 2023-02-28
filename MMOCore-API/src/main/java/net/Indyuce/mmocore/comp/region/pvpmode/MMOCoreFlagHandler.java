package net.Indyuce.mmocore.comp.region.pvpmode;

import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.FlagValueChangeHandler;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class MMOCoreFlagHandler extends FlagValueChangeHandler<StateFlag.State> {

    @NotNull
    protected PlayerData playerData;

    protected long lastMessage;

    protected static final long MESSAGE_TIMEOUT = 3 * 1000;

    public MMOCoreFlagHandler(Session session, Flag<StateFlag.State> flag) {
        super(session, flag);
    }

    /**
     * Triggered when WorldGuard initializes the value for the first time,
     * on player login or world change for instance.
     */
    @Override
    protected void onInitialValue(LocalPlayer player, ApplicableRegionSet set, StateFlag.State value) {
        try {
            playerData = PlayerData.get(player.getUniqueId());

            // Things get done here
            onSetValue(player, player.getLocation(), player.getLocation(), set, value, StateFlag.State.DENY, MoveType.TELEPORT);
        } catch (Exception exception) {
            // Citizens.
        }
    }

    public abstract StateFlag.State getDefaultState();

    /**
     * Triggered when WorldGuard does not find a region setting the value of the flag.
     * In that case, put PvP mode to its default setting that is OFF.
     */
    @Override
    protected boolean onAbsentValue(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, StateFlag.State lastValue, MoveType moveType) {
        return onSetValue(player, from, to, toSet, getDefaultState(), lastValue, moveType);
    }

    protected boolean isInvalid() {
        return playerData == null;
    }

    protected boolean toBoolean(@Nullable StateFlag.State state) {
        return state == StateFlag.State.ALLOW;
    }

    protected boolean canSendMessage() {
        return System.currentTimeMillis() > lastMessage + MESSAGE_TIMEOUT;
    }
}
