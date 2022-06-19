package fr.phoenix.mmocore.bungee;

import java.util.HashMap;
import java.util.UUID;

public class CacheManager {
    private final HashMap<UUID,String> cachedPlayers= new HashMap<>();


    public String getCachedPlayer(UUID uuid) {
        return cachedPlayers.get(uuid);
    }
    public boolean hasCachedPlayer(UUID uuid) {
        return cachedPlayers.containsKey(uuid);
    }

    public void addCachedPlayer(UUID uuid,String playerData) {
        cachedPlayers.put(uuid,playerData);
    }


    public void save() {
    }
}
