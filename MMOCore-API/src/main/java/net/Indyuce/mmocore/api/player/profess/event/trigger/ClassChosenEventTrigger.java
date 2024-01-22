package net.Indyuce.mmocore.api.player.profess.event.trigger;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.event.PlayerChangeClassEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.event.EventTriggerHandler;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

@Deprecated
public class ClassChosenEventTrigger implements EventTriggerHandler {

	@Override
	public boolean handles(String event) {
		return event.startsWith("class-chosen");
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void a(PlayerChangeClassEvent event) {
		PlayerData player = event.getData();
		if (event.getNewClass().hasEventTriggers("class-chosen")) {
			Bukkit.getScheduler().runTaskLater(MMOCore.plugin, () -> {
				event.getNewClass().getEventTriggers("class-chosen").getTriggers().forEach(trigger -> trigger.apply(player));
			}, 1);

		}
	}
}
