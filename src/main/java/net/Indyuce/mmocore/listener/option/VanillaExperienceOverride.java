package net.Indyuce.mmocore.listener.option;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;

import net.Indyuce.mmocore.MMOCore;

public class VanillaExperienceOverride implements Listener {
	
	@EventHandler
	public void a(PlayerExpChangeEvent event) {
		if (MMOCore.plugin.configManager.overrideVanillaExp)
			event.setAmount(0);
	}
}
