package net.Indyuce.mmocore.listener.option;

import net.Indyuce.mmocore.MMOCore;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.metadata.FixedMetadataValue;

public class NoSpawnerEXP implements Listener {
	@EventHandler
	public void a(CreatureSpawnEvent event) {
		if(event.getSpawnReason() == SpawnReason.SPAWNER)
			event.getEntity().setMetadata("spawner_spawned", new FixedMetadataValue(MMOCore.plugin, true));
	}
}
