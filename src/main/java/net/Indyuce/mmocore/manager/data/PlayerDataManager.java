package net.Indyuce.mmocore.manager.data;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import lombok.Getter;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.event.PlayerDataLoadEvent;
import net.Indyuce.mmocore.api.player.OfflinePlayerData;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.mmogroup.mmolib.api.player.MMOPlayerData;

public abstract class PlayerDataManager {
	private DefaultPlayerData defaultData = new DefaultPlayerData();

	public PlayerData get(OfflinePlayer player) {
		return get(player.getUniqueId());
	}

	public PlayerData get(UUID uuid) {
		PlayerData found = MMOPlayerData.get(uuid).getMMOCore();
		return found == null ? PlayerData.NOT_LOADED : found;
	}

	public void remove(UUID uuid) {
		if (MMOPlayerData.isLoaded(uuid))
			MMOPlayerData.get(uuid).setMMOCore(null);
	}

	public abstract OfflinePlayerData getOffline(UUID uuid);

	public void setup(Player player) {

		/*
		 * Setup playerData based on loadData method to support both MySQL and
		 * YAML data storage
		 */
		MMOPlayerData mmoData = MMOPlayerData.get(player);
		if (mmoData.getMMOCore() == null) {
			PlayerData generated = new PlayerData(mmoData);

			/*
			 * Loads player data and ONLY THEN refresh the player statistics and
			 * calls the load event on the MAIN thread
			 */
			Bukkit.getScheduler().runTaskAsynchronously(MMOCore.plugin, () -> {
				loadData(generated);
				Bukkit.getScheduler().runTask(MMOCore.plugin, () -> Bukkit.getPluginManager().callEvent(new PlayerDataLoadEvent(generated)));
				generated.getStats().updateStats();
			});
		}
	}

	public DefaultPlayerData getDefaultData() {
		return defaultData;
	}

	public void loadDefaultData(ConfigurationSection config) {
		defaultData = new DefaultPlayerData(config);
	}

	public boolean isLoaded(UUID uuid) {
		return MMOPlayerData.isLoaded(uuid) && MMOPlayerData.get(uuid).getMMOCore() != null;
	}

	public Collection<PlayerData> getLoaded() {
		return MMOPlayerData.getLoaded().stream().filter(data -> data.getMMOCore() != null).map(data -> data.getMMOCore())
				.collect(Collectors.toSet());
	}

	public abstract void loadData(PlayerData data);

	public abstract void saveData(PlayerData data);

	public abstract void remove(PlayerData data);

	@Getter
	public class DefaultPlayerData {
		private final int level, classPoints, skillPoints, attributePoints, attrReallocPoints;

		public DefaultPlayerData(ConfigurationSection config) {
			level = config.getInt("level", 1);
			classPoints = config.getInt("class-points");
			skillPoints = config.getInt("skill-points");
			attributePoints = config.getInt("attribute-points");
			attrReallocPoints = config.getInt("attribute-realloc-points");
		}

		public DefaultPlayerData() {
			level = 1;
			classPoints = 0;
			skillPoints = 0;
			attributePoints = 0;
			attrReallocPoints = 0;
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

		public int getAttributeReallocationPoints() {
			return attrReallocPoints;
		}

		public int getAttributePoints() {
			return attributePoints;
		}
	}
}
