package net.Indyuce.mmocore.api.player.profess.event;

import org.bukkit.event.Listener;

@Deprecated
public interface EventTriggerHandler extends Listener {
	boolean handles(String event);
}
