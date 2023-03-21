package net.Indyuce.mmocore.api.quest.trigger;

import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.api.stat.modifier.StatModifier;
import io.lumine.mythic.lib.player.modifier.ModifierType;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.quest.trigger.api.Removable;
import org.apache.commons.lang.Validate;

import java.util.UUID;

public class StatTrigger extends Trigger implements Removable {
    public static String TRIGGER_PREFIX = "mmocore_trigger";
    private final StatModifier statModifier;
    private final String stat;
    private final String modifierKey = TRIGGER_PREFIX + "." + UUID.randomUUID();
    private final double amount;

    public StatTrigger(MMOLineConfig config) {
        super(config);

        config.validateKeys("amount");
        config.validateKeys("stat");
        config.validateKeys("type");
        String type = config.getString("type").toUpperCase();
        Validate.isTrue(type.equals("FLAT") || type.equals("RELATIVE"));
        stat = config.getString("stat");
        amount = config.getDouble("amount");
        statModifier = new StatModifier(modifierKey, stat, amount, ModifierType.valueOf(type));
    }

    @Override
    public void apply(PlayerData player) {
        StatModifier prevModifier = player.getMMOPlayerData().getStatMap().getInstance(stat).getModifier(modifierKey);
        if (prevModifier == null)
            statModifier.register(player.getMMOPlayerData());
        else
            prevModifier.add(amount).register(player.getMMOPlayerData());
    }

    /**
     * Removes the effect of the trigger to the player by registering the
     * opposite amount. (Little corrective term for the relative to have the inverse.
     * Not a problem to store twice the stat modifiers are there only remain in the RAM.
     */
    @Override
    public void remove(PlayerData playerData) {
        playerData.getMMOPlayerData().getStatMap().getInstance(stat).remove(modifierKey);
    }
}
