package net.Indyuce.mmocore.api.player.profess.event;

import net.Indyuce.mmocore.experience.droptable.ExperienceTable;
import org.bukkit.event.Listener;

/**
 * @deprecated Replaced by {@link ExperienceTable} and will
 *         be removed in 1.8.4
 */
@Deprecated
public interface EventTriggerHandler extends Listener {
	boolean handles(String event);
}
