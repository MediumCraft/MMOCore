package net.Indyuce.mmocore.bungee;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.manager.data.mysql.MySQLTableEditor;

import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Collectors;

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
