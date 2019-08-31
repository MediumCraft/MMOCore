package net.Indyuce.mmocore.api.player;

import java.util.List;
import java.util.UUID;

import net.Indyuce.mmocore.api.ConfigFile;

public class OfflinePlayerData {
	private final UUID uuid;
	private final PlayerData data;

	/*
	 * supports offline player data operations like friend removals which can't
	 * be handled when their player data is not loaded in the data map.
	 */
	public OfflinePlayerData(UUID uuid) {
		data = PlayerData.isLoaded(this.uuid = uuid) ? PlayerData.get(uuid) : null;
	}

	public boolean isLoaded() {
		return data != null;
	}

	public void removeFriend(UUID uuid) {
		if (isLoaded()) {
			data.removeFriend(uuid);
			return;
		}

		ConfigFile config = new ConfigFile(this.uuid);
		List<String> friends = config.getConfig().getStringList("friends");
		friends.remove(uuid.toString());
		config.getConfig().set("friends", friends);
		config.save();
	}

	public boolean hasFriend(UUID uuid) {
		return isLoaded() ? data.hasFriend(uuid) : new ConfigFile(this.uuid).getConfig().getStringList("friends").contains(uuid.toString());
	}
}
