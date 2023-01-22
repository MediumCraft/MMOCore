package net.Indyuce.mmocore.manager.data;

import io.lumine.mythic.lib.api.player.MMOPlayerData;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.event.AsyncPlayerDataLoadEvent;
import net.Indyuce.mmocore.api.event.PlayerDataLoadEvent;
import net.Indyuce.mmocore.api.player.OfflinePlayerData;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.player.DefaultPlayerData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class PlayerDataManager {
    private final static Map<UUID, PlayerData> data = Collections.synchronizedMap(new HashMap<>());

    private DefaultPlayerData defaultData = DefaultPlayerData.DEFAULT;

    public PlayerData get(OfflinePlayer player) {
        return get(player.getUniqueId());
    }

    /**
     * Gets the player data, or throws an exception if not found.
     * The player data should be loaded when the player logs in
     * so it's really bad practice to setup the player data if it's not loaded.
     *
     * @param uuid Player UUID
     * @return Player data, if it's loaded
     */
    public PlayerData get(UUID uuid) {
        return Objects.requireNonNull(data.get(uuid), "Player data is not loaded");
    }

    /**
     * Safely unregisters the player data from the map.
     * This saves the player data either through SQL or YAML,
     * then closes the player data and clears it from the data map.
     *
     * @param playerData PLayer data to unregister
     */
    public void unregisterSafe(PlayerData playerData) {

        // Save data async if required
        if (playerData.isFullyLoaded())
            Bukkit.getScheduler().runTaskAsynchronously(MMOCore.plugin, () -> saveData(playerData, true));

        // Close and unregister data instantly if no error occured
        playerData.close();
        data.remove(playerData.getUniqueId());
    }

    /**
     * Offline player data is used to handle processes like friend removal
     * which can still occur if one of the two players is offline.
     * <p>
     * Unlike {@link #get(UUID)} this method never returns a null instance
     *
     * @param uuid Player unique id
     * @return Offline player data
     */
    @NotNull
    public abstract OfflinePlayerData getOffline(UUID uuid);

    /**
     * Called when a player logs in, loading the player data inside the map.
     * <p>
     * For YAML configs or SQL databases, data is loaded as not to overload
     * the main thread with SQL requests. Therefore, the player data returned
     * by that method, when the player joined for the first time, is not
     * fully loaded YET.
     *
     * @param uniqueId Player UUID
     * @return The loaded player data.
     */
    public PlayerData setup(UUID uniqueId) {

        // Load player data if it does not exist
        if (!data.containsKey(uniqueId)) {
            PlayerData newData = new PlayerData(MMOPlayerData.get(uniqueId));

            // Schedule async data loading
            Bukkit.getScheduler().runTaskAsynchronously(MMOCore.plugin, () -> {
                loadData(newData);
                newData.getStats().updateStats();
                Bukkit.getPluginManager().callEvent(new AsyncPlayerDataLoadEvent(newData));
                Bukkit.getScheduler().runTask(MMOCore.plugin, () -> Bukkit.getPluginManager().callEvent(new PlayerDataLoadEvent(newData)));
            });

            // Update data map
            data.put(uniqueId, newData);

            return newData;
        }

        return data.get(uniqueId);
    }

    public DefaultPlayerData getDefaultData() {
        return defaultData;
    }

    public void loadDefaultData(ConfigurationSection config) {
        defaultData = new DefaultPlayerData(config);
    }

    public boolean isLoaded(UUID uuid) {
        return data.containsKey(uuid);
    }

    public Collection<PlayerData> getLoaded() {
        return data.values();
    }

    public abstract void loadData(PlayerData data);

    /**
     * Called when player data must be saved in configs or database.
     * This method should always be called sync because it DOES register
     * an async task in case MySQL storage is used.
     *
     * @param data   Player data to save
     * @param logout When logging out, is_saved is switched back to 1. This parameter
     *               must be turned off when auto-saving because the player doesn't
     *               actually leave the server.
     */
    public abstract void saveData(PlayerData data, boolean logout);
}
