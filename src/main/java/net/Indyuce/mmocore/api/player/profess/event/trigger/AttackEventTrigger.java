package net.Indyuce.mmocore.api.player.profess.event.trigger;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import net.Indyuce.mmocore.api.event.PlayerAttackEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.PlayerClass;
import net.Indyuce.mmocore.api.player.profess.event.EventTriggerHandler;
import net.mmogroup.mmolib.api.DamageType;

public class AttackEventTrigger implements EventTriggerHandler {

	@Override
	public boolean handles(String event) {
		return event.endsWith("-damage");
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void a(PlayerAttackEvent event) {
		PlayerData player = event.getData();
		PlayerClass profess = player.getProfess();

		for (DamageType type : event.getAttackInfo().getTypes()) {
			String path = type.getPath() + "-damage";
			if (profess.hasEventTriggers(path))
				profess.getEventTriggers(path).getTriggers().forEach(trigger -> trigger.apply(player));
		}
	}
}
