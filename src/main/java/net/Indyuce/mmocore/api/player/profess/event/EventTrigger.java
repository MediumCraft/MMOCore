package net.Indyuce.mmocore.api.player.profess.event;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.load.MMOLoadException;
import net.Indyuce.mmocore.api.quest.trigger.Trigger;
import net.Indyuce.mmoitems.api.util.MMOLineConfig;

public class EventTrigger {
	private final String event;
	private final Set<Trigger> triggers = new LinkedHashSet<>();

	public EventTrigger(String event, List<String> list) {
		Validate.notNull(list, "Could not load trigger list");
		
		this.event = event;

		for (String format : list)
			try {
				triggers.add(MMOCore.plugin.loadManager.loadTrigger(new MMOLineConfig(format)));
			} catch (MMOLoadException exception) {
				exception.printConsole("EventTrigger:" + event, "trigger");
			}
	}
	
	public String getEvent() {
		return event;
	}

	public Set<Trigger> getTriggers() {
		return triggers;
	}
}
