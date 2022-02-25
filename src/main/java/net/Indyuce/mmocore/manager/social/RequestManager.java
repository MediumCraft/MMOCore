package net.Indyuce.mmocore.manager.social;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.social.Request;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class RequestManager {
    private final Map<UUID, Request> requests = new HashMap<>();

    /**
     * Flushes friend requests every 5 minutes so there is no memory overleak
     */
    public RequestManager() {
        Bukkit.getScheduler().runTaskTimer(MMOCore.plugin, this::flushRequests, 60 * 20, 60 * 20 * 5);
    }

    public Request getRequest(UUID uuid) {
        return requests.get(uuid);
    }

    public void registerRequest(Request request) {
        requests.put(request.getUniqueId(), request);
    }

    public void unregisterRequest(UUID uuid) {
        requests.remove(uuid);
    }

    private void flushRequests() {
        for (Iterator<Request> iterator = requests.values().iterator(); iterator.hasNext(); ) {
            Request next = iterator.next();
            if (next.isTimedOut())
                iterator.remove();
        }
    }
}
