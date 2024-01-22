package net.Indyuce.mmocore.listener.option;

import net.Indyuce.mmocore.MMOCore;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;

public class NoSpawnerEXP implements Listener {
	@EventHandler
	public void onSpawn(CreatureSpawnEvent event) {
		if(event.getSpawnReason() == SpawnReason.SPAWNER) {
			event.getEntity().getPersistentDataContainer().set(new NamespacedKey(MMOCore.plugin,"spawner_spawned"), PersistentDataType.STRING,"true");
		}
	}
}
