package net.Indyuce.mmocore.api.player.profess.event;

import org.bukkit.event.Listener;

public interface EventTriggerHandler extends Listener {
	boolean handles(String event);
}
