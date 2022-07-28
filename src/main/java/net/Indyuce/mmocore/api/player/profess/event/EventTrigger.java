package net.Indyuce.mmocore.api.player.profess.event;

import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.quest.trigger.Trigger;
import org.apache.commons.lang.Validate;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

/**
 * @deprecated Should have NEVER been implemented that way
 */
@Deprecated
public class EventTrigger {
    private final String event;
    private final Set<Trigger> triggers = new LinkedHashSet<>();

    public EventTrigger(String event, List<String> list) {
        Validate.notNull(list, "Could not load trigger list");

        this.event = event;

        for (String format : list)
            try {
                triggers.addAll(MMOCore.plugin.loadManager.loadTrigger(new MMOLineConfig(format)));
            } catch (IllegalArgumentException exception) {
                MMOCore.plugin.getLogger().log(Level.WARNING,
                        "Could not load trigger '" + format + "' from event trigger '" + event + "': " + exception.getMessage());
            }
    }

    public String getEvent() {
        return event;
    }

    public Set<Trigger> getTriggers() {
        return triggers;
    }
}
