package net.Indyuce.mmocore.api.player.profess.event.trigger;

import net.Indyuce.mmocore.api.event.PlayerLevelUpEvent;
import net.Indyuce.mmocore.api.player.profess.PlayerClass;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.event.EventTriggerHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.text.DecimalFormat;

@Deprecated
public class MultipleLevelUpEventTrigger implements EventTriggerHandler {

    @Override
    public boolean handles(String event) {
        return event.startsWith("level-up-multiple");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void a(PlayerLevelUpEvent event) {
        PlayerData player = event.getData();
        PlayerClass profess = player.getProfess();

		for(int i = event.getOldLevel(); i < event.getNewLevel(); i++) {
			int level = i + 1;

	        for (String t : profess.getEventTriggers()){
	            if (t.startsWith("level-up-multiple")) {
	                String[] split = t.split("-");
	                double multiple = Double.parseDouble(split[split.length-1]);
	                if (level / multiple % 1 == 0) {
	                    DecimalFormat f = new DecimalFormat("#");
	                    if (event.hasProfession()) {
	                        processTrigger(player, profess, "level-up-multiple-" + event.getProfession().getId().toLowerCase() + "-" + f.format(multiple));
	                    } else {
	                        processTrigger(player, profess, "level-up-multiple-" + f.format(multiple));
	                    }
	                }
	            }
	        }
		}
    }

    public void processTrigger(PlayerData player, PlayerClass profess, String trigger) {
        if(profess.hasEventTriggers(trigger)) profess.getEventTriggers(trigger).getTriggers().forEach(t -> t.apply(player));
    }
}
