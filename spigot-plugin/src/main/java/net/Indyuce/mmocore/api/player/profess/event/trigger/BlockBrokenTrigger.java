package net.Indyuce.mmocore.api.player.profess.event.trigger;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.event.EventTriggerHandler;

@Deprecated
public class BlockBrokenTrigger implements EventTriggerHandler {

	@Override
	public boolean handles(String event) {
		return event.startsWith("break-block");
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void a(BlockBreakEvent event) {
		PlayerData player = PlayerData.get(event.getPlayer());
		if (player.getProfess().hasEventTriggers("break-block"))
			player.getProfess().getEventTriggers("break-block").getTriggers().forEach(trigger -> trigger.apply(player));
	}
}
