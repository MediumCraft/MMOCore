package net.Indyuce.mmocore.manager.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.OfflinePlayerData;
import net.Indyuce.mmocore.api.player.PlayerData;

public abstract class PlayerDataManager {
	private final Map<UUID, PlayerData> map = new HashMap<>();

	public PlayerData get(OfflinePlayer player) {
		return get(player.getUniqueId());
	}

	public PlayerData get(UUID uuid) {
		return map.get(uuid);
	}

	public abstract OfflinePlayerData getOffline(UUID uuid);

	public void setup(Player player) {

		/*
		 * setup playerData based on loadData method to support both MySQL and
		 * YAML data storage
		 */
		if (!map.containsKey(player.getUniqueId())) {
			PlayerData generated = new PlayerData(player);
			map.put(player.getUniqueId(), generated);

			/*
			 * loads player data and ONLY THEN refresh the player statistics
			 */
			Bukkit.getScheduler().runTaskAsynchronously(MMOCore.plugin, () -> {
				loadData(generated);
				generated.getStats().updateStats();
			});
		}

		get(player).setPlayer(player);
	}

	public boolean isLoaded(UUID uuid) {
		return map.containsKey(uuid);
	}

	public Collection<PlayerData> getLoaded() {
		return map.values();
	}

	public abstract void loadData(PlayerData data);

	public abstract void saveData(PlayerData data);
}
