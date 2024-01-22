package net.Indyuce.mmocore.api.player.profess.event.trigger;

import net.Indyuce.mmocore.api.event.PlayerLevelUpEvent;
import net.Indyuce.mmocore.api.player.profess.PlayerClass;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.event.EventTriggerHandler;

@Deprecated
public class LevelUpEventTrigger implements EventTriggerHandler {

	@Override
	public boolean handles(String event) {
		return event.startsWith("level-up");
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void a(PlayerLevelUpEvent event) {
		PlayerData player = event.getData();
		PlayerClass profess = player.getProfess();

		for(int i = event.getOldLevel(); i < event.getNewLevel(); i++) {
			int level = i + 1;
			if(event.hasProfession()) {
				String prof = event.getProfession().getId().toLowerCase();
				processTrigger(player, profess, "level-up-" + prof);
				processTrigger(player, profess, "level-up-" + prof + "-" + level);
			} else {
				processTrigger(player, profess, "level-up");
				processTrigger(player, profess, "level-up-" + level);
				if(profess.getMaxLevel() == level)
					processTrigger(player, profess, "level-up-max");
			}
		}
	}
	
	public void processTrigger(PlayerData player, PlayerClass profess, String trigger) {
		if(profess.hasEventTriggers(trigger)) profess.getEventTriggers(trigger).getTriggers().forEach(t -> t.apply(player));
	}
}
