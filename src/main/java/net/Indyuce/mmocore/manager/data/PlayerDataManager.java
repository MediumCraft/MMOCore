package net.Indyuce.mmocore.manager.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.event.PlayerDataLoadEvent;
import net.Indyuce.mmocore.api.player.OfflinePlayerData;
import net.Indyuce.mmocore.api.player.PlayerData;
import io.lumine.mythic.lib.api.player.MMOPlayerData;

public abstract class PlayerDataManager {
	private final static Map<UUID, PlayerData> data = new HashMap<>();
	private DefaultPlayerData defaultData = new DefaultPlayerData(1, 0, 0, 0, 0);

	public PlayerData get(OfflinePlayer player) {
		return get(player.getUniqueId());
	}

	public PlayerData get(UUID uuid) {
		return data.getOrDefault(uuid, setup(uuid));
	}

	public void remove(UUID uuid) {
		data.remove(uuid);
	}

	public abstract OfflinePlayerData getOffline(UUID uuid);

	public PlayerData setup(UUID uuid) {
		/*
		 * Setup playerData based on loadData method to support both MySQL and
		 * YAML data storage
		 */
		PlayerData playerData = data.get(uuid);
		if (playerData == null) {
			playerData = data.put(uuid, new PlayerData(MMOPlayerData.get(uuid)));

			/*
			 * Loads player data and ONLY THEN refresh the player statistics and
			 * calls the load event on the MAIN thread
			 */
			Bukkit.getScheduler().runTaskAsynchronously(MMOCore.plugin, () -> {
				PlayerData loaded = PlayerData.get(uuid);
				if (!loaded.isOnline())
					return;
				loadData(loaded);
				Bukkit.getScheduler().runTask(MMOCore.plugin, () -> {
					if (loaded.isOnline())
						Bukkit.getPluginManager().callEvent(new PlayerDataLoadEvent(loaded));
				});
				loaded.getStats().updateStats();
			});
		}
		return playerData;
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

	public abstract void saveData(PlayerData data);

	public abstract void remove(PlayerData data);

	public static class DefaultPlayerData {
		private final int level, classPoints, skillPoints, attributePoints, attrReallocPoints;

		public DefaultPlayerData(ConfigurationSection config) {
			level = config.getInt("level", 1);
			classPoints = config.getInt("class-points");
			skillPoints = config.getInt("skill-points");
			attributePoints = config.getInt("attribute-points");
			attrReallocPoints = config.getInt("attribute-realloc-points");
		}

		public DefaultPlayerData(int level, int classPoints, int skillPoints, int attributePoints, int attrReallocPoints) {
			this.level = level;
			this.classPoints = classPoints;
			this.skillPoints = skillPoints;
			this.attributePoints = attributePoints;
			this.attrReallocPoints = attrReallocPoints;
		}

		public int getLevel() {
			return level;
		}

		public int getSkillPoints() {
			return skillPoints;
		}

		public int getClassPoints() {
			return classPoints;
		}

		public int getAttrReallocPoints() {
			return attrReallocPoints;
		}

		public int getAttributePoints() {
			return attributePoints;
		}

		public void apply(PlayerData player) {
			player.setLevel(level);
			player.setClassPoints(classPoints);
			player.setSkillPoints(skillPoints);
			player.setAttributePoints(attributePoints);
			player.setAttributeReallocationPoints(attrReallocPoints);
		}
	}
}
