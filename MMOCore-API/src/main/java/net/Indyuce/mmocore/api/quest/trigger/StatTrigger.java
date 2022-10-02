package net.Indyuce.mmocore.api.quest.trigger;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.api.stat.modifier.StatModifier;
import io.lumine.mythic.lib.player.modifier.ModifierType;
import io.lumine.mythic.lib.player.modifier.PlayerModifier;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;

import java.util.Collection;

public class StatTrigger extends Trigger {
    private final String stat;
    private final double amount;
    private final ModifierType type;
    private double totalAmount;

    public StatTrigger(MMOLineConfig config) {
        super(config);

        config.validateKeys("amount");
        config.validateKeys("stat");
        config.validateKeys("type");
        String type = config.getString("type").toUpperCase();
        Validate.isTrue(type.equals("FLAT") || type.equals("RELATIVE"));
        stat = config.getString("stat");
        amount = config.getDouble("amount");
        this.type = ModifierType.valueOf(type);
        this.totalAmount = 0;
    }

    @Override
    public void apply(PlayerData player) {
        totalAmount+=amount;
        new StatModifier("trigger",stat,totalAmount,type).register(player.getMMOPlayerData());
    }


    /**
     * Removes the effect of the trigger to the player by registering the
     * opposite amount. (Little corrective term for the relative to have the inverse.
     * Not a problem to store twice the stat modifiers are there only remain in the RAM.
     */
    public void remove(PlayerData playerData) {
        totalAmount-=amount;
        new StatModifier("trigger", stat, totalAmount, type).register(playerData.getMMOPlayerData());
    }
}
