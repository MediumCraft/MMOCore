package net.Indyuce.mmocore.comp.citizens;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import net.citizensnpcs.api.CitizensAPI;

public class CitizenInteractEventListener implements Listener {

	@EventHandler
	public void a(PlayerInteractEntityEvent event) {
		Entity entity = event.getRightClicked();
		if (CitizensAPI.getNPCRegistry().isNPC(entity))
			Bukkit.getPluginManager().callEvent(new CitizenInteractEvent(event.getPlayer(), CitizensAPI.getNPCRegistry().getNPC(entity)));
	}
}
