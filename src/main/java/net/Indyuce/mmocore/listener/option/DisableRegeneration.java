package net.Indyuce.mmocore.listener.option;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;

public class DisableRegeneration implements Listener {

	@EventHandler(priority = EventPriority.HIGH)
	public void a(EntityRegainHealthEvent event) {
		if (event.getEntityType() == EntityType.PLAYER)
			if (event.getRegainReason() == RegainReason.SATIATED || event.getRegainReason() == RegainReason.REGEN || event.getRegainReason() == RegainReason.EATING)
				event.setCancelled(true);
	}
}
