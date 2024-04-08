package net.Indyuce.mmocore.api.quest.trigger;

import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.api.stat.modifier.StatModifier;
import io.lumine.mythic.lib.player.modifier.ModifierType;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.quest.trigger.api.Removable;
import net.Indyuce.mmocore.api.quest.trigger.api.Temporary;
import org.apache.commons.lang.Validate;

public class StatTrigger extends Trigger implements Removable, Temporary {
    private final StatModifier modifier;
    private final String stat;
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
        modifier = new StatModifier(Trigger.STAT_MODIFIER_KEY, stat, amount, ModifierType.valueOf(type));
    }

    @Override
    public void apply(PlayerData player) {
        StatModifier prevModifier = player.getMMOPlayerData().getStatMap().getInstance(stat).getModifier(modifier.getUniqueId());
        if (prevModifier == null) modifier.register(player.getMMOPlayerData());
        else prevModifier.add(amount).register(player.getMMOPlayerData());
    }

    @Override
    public void remove(PlayerData playerData) {
        modifier.unregister(playerData.getMMOPlayerData());
    }
}
