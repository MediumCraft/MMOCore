package net.Indyuce.mmocore.api.player.profess.event.trigger;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.event.EventTriggerHandler;

@Deprecated
public class BlockPlacedTrigger implements EventTriggerHandler {

	@Override
	public boolean handles(String event) {
		return event.startsWith("place-block");
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void a(BlockPlaceEvent event) {
		PlayerData player = PlayerData.get(event.getPlayer());
		if (player.getProfess().hasEventTriggers("place-block"))
			player.getProfess().getEventTriggers("place-block").getTriggers().forEach(trigger -> trigger.apply(player));
	}
}
