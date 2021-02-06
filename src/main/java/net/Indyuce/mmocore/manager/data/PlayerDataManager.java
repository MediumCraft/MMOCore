package net.Indyuce.mmocore.manager.data;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.event.PlayerDataLoadEvent;
import net.Indyuce.mmocore.api.player.OfflinePlayerData;
import net.Indyuce.mmocore.api.player.PlayerData;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class PlayerDataManager {
	private final static Map<UUID, PlayerData> data = Collections.synchronizedMap(new HashMap<>());

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

	public PlayerData setup(UUID uniqueId) {
		return data.compute(uniqueId, (uuid, searchData) -> {
			if (searchData == null) {
				PlayerData playerData = new PlayerData(MMOPlayerData.get(uniqueId));

				loadData(playerData);
				playerData.getStats().updateStats();

				// We call the player data load event. TODO: Convert this event to async.
				new BukkitRunnable() {
					@Override
					public void run() {
						Bukkit.getPluginManager().callEvent(new PlayerDataLoadEvent(playerData));
					}
				}.runTask(MMOCore.plugin);

				return playerData;
			} else return searchData;

		});
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
