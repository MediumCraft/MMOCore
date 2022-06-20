package net.Indyuce.mmocore.manager.social;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.social.Request;
import net.Indyuce.mmocore.manager.MMOCoreManager;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class RequestManager implements MMOCoreManager {
    private final Map<UUID, Request> requests = new HashMap<>();

    private boolean ENABLED;

    @NotNull
    public Request getRequest(UUID uuid) {
        return Objects.requireNonNull(requests.get(uuid), "Could not find request with UUID '" + uuid.toString() + "'");
    }

    public void registerRequest(Request request) {
        requests.put(request.getUniqueId(), request);
    }

    @Nullable
    public Request unregisterRequest(UUID uuid) {
        return requests.remove(uuid);
    }

    /**
     * Flushes friend, guild or party invites every 5 minutes to prevent memory leaks
     */
    private void flushRequests() {
        for (Iterator<Request> iterator = requests.values().iterator(); iterator.hasNext(); ) {
            Request next = iterator.next();
            if (next.isTimedOut())
                iterator.remove();
        }
    }

    /**
     * The request map is NOT cleared, whatever state of the <code>clearBefore</code>
     * boolean as it's useless because they are guaranteed to disappear with time.
     *
     * @param clearBefore Useless here
     */
    @Override
    public void initialize(boolean clearBefore) {
        if (!ENABLED) {
            Bukkit.getScheduler().runTaskTimer(MMOCore.plugin, this::flushRequests, 60 * 20, 60 * 20 * 5);
            ENABLED = true;
        }
    }
}
