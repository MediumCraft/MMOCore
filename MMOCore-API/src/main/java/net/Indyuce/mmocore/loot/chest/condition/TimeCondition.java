package net.Indyuce.mmocore.loot.chest.condition;

import io.lumine.mythic.lib.api.MMOLineConfig;
import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;

public class TimeCondition extends Condition {

    private final int min;
    private final int max;

    public TimeCondition(MMOLineConfig config) {
        super(config);

        Validate.isTrue(config.contains("min"));
        Validate.isTrue(config.contains("max"));

        min = config.getInt("min");
        max = config.getInt("max");
    }

    @Override
    public boolean isMet(ConditionInstance entity) {
        long time = entity.getLocation().getWorld().getTime();

        if (min < max) {
            return time > min && time < max;
        } else {
            // Allows for wrapping times, such as min=20000 max=6000
            return time > min || time < max;
        }
    }
}
