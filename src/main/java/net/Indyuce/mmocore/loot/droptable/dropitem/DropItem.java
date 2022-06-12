package net.Indyuce.mmocore.loot.droptable.dropitem;

import java.util.Random;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.loot.LootBuilder;
import net.Indyuce.mmocore.api.util.math.formula.RandomAmount;
import io.lumine.mythic.lib.api.MMOLineConfig;

public abstract class DropItem {
    protected static final Random random = new Random();

    private final double chance, weight;
    private final RandomAmount amount;

    public DropItem(MMOLineConfig config) {
        chance = config.args().length > 0 ? Double.parseDouble(config.args()[0]) : 1;
        amount = config.args().length > 1 ? new RandomAmount(config.args()[1]) : new RandomAmount(1, 1);
        weight = config.args().length > 2 ? Double.parseDouble(config.args()[2]) : 0;
    }

    public RandomAmount getAmount() {
        return amount;
    }

    public double getChance() {
        return chance;
    }

    public double getWeight() {
        return weight;
    }

    public int rollAmount() {
        return amount.calculateInt();
    }

    /**
     * If the player chance is 0 the random value will remain the same. When he get lucks the chance gets closer to one.
     */
    public boolean rollChance(PlayerData player) {
        return Math.pow(random.nextDouble(), 1 / Math.log(1 + player.getStats().getStat("CHANCE"))) < chance;
    }

    public abstract void collect(LootBuilder builder);
}
