package net.Indyuce.mmocore.comp.citizens;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.comp.entity.EntityHandler;
import net.citizensnpcs.api.CitizensAPI;

public class CitizenInteractEventListener implements Listener, EntityHandler {

	public CitizenInteractEventListener() {

		/*
		 * prevents NPCs from being skill targets.
		 */
		MMOCore.plugin.entities.registerHandler(this);
	}

	@EventHandler
	public void a(PlayerInteractEntityEvent event) {
		Entity entity = event.getRightClicked();
		if (CitizensAPI.getNPCRegistry().isNPC(entity))
			Bukkit.getPluginManager().callEvent(new CitizenInteractEvent(event.getPlayer(), CitizensAPI.getNPCRegistry().getNPC(entity)));
	}

	@Override
	public boolean isCustomEntity(Entity entity) {
		return entity.hasMetadata("NPC");
	}
}
