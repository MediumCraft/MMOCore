package net.Indyuce.mmocore.api.player.profess.event.trigger;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import net.Indyuce.mmocore.api.event.PlayerLevelUpEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.PlayerClass;
import net.Indyuce.mmocore.api.player.profess.event.EventTriggerHandler;

public class LevelUpEventTrigger implements EventTriggerHandler {

	@Override
	public boolean handles(String event) {
		return event.startsWith("level-up");
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void a(PlayerLevelUpEvent event) {
		PlayerData player = event.getData();
		PlayerClass profess = player.getProfess();

		if (event.hasProfession())
			for (String event1 : profess.getEventTriggers())
			{
				if (event1.startsWith("level-up-") && event1.substring(9).equalsIgnoreCase(event.getProfession().getId())) {
					profess.getEventTriggers(event1).getTriggers().forEach(trigger -> trigger.apply(player));
					break;
				}
				if (event1.startsWith("level-up-") && event1.substring(9).equalsIgnoreCase(event.getProfession().getId() + "-" + event.getNewLevel())) {
					profess.getEventTriggers(event1).getTriggers().forEach(trigger -> trigger.apply(player));
					break;
				}
			}
		
		if (!event.hasProfession() && profess.hasEventTriggers("level-up"))
			profess.getEventTriggers("level-up").getTriggers().forEach(trigger -> trigger.apply(player));
		if (!event.hasProfession() && profess.hasEventTriggers("level-up-" + event.getNewLevel()))
			profess.getEventTriggers("level-up-" + event.getNewLevel()).getTriggers().forEach(trigger -> trigger.apply(player));
	}
}
