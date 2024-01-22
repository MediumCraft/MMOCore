package net.Indyuce.mmocore.skill.trigger;

import io.lumine.mythic.lib.skill.trigger.TriggerType;
import org.jetbrains.annotations.NotNull;

public class MMOCoreTriggerType {

    /**
     * Called when a player enters combat
     */
    @NotNull
    public static TriggerType ENTER_COMBAT = new TriggerType("ENTER_COMBAT"),

    /**
     * Called when a player quits combat
     */
    QUIT_COMBAT = new TriggerType("QUIT_COMBAT");

    public static void registerAll() {
        TriggerType.register(ENTER_COMBAT);
        TriggerType.register(QUIT_COMBAT);
    }
}
