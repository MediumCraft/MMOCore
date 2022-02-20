package net.Indyuce.mmocore.api.player.social;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.apache.commons.lang.Validate;

import java.util.UUID;

public abstract class Request {
    private final UUID uuid = UUID.randomUUID();
    private final long date = System.currentTimeMillis();
    private final PlayerData creator, target;

    /**
     * Any request time out is by default 2 minutes.
     */
    private static final long TIME_OUT = 1000 * 60 * 2;

    public Request(PlayerData creator, PlayerData target) {
        this.creator = creator;
        this.target = target;
    }

    public PlayerData getCreator() {
        return creator;
    }

    public PlayerData getTarget() {
        return target;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public boolean isTimedOut() {
        return date + TIME_OUT < System.currentTimeMillis();
    }

    public void accept() {
        Validate.isTrue(target.isOnline(), "Target must be online");
        whenAccepted();
        MMOCore.plugin.requestManager.unregisterRequest(getUniqueId());
    }

    public abstract void whenAccepted();

    public void deny() {
        Validate.isTrue(target.isOnline(), "Target must be online");
        whenDenied();
        MMOCore.plugin.requestManager.unregisterRequest(getUniqueId());
    }

    public abstract void whenDenied();
}
