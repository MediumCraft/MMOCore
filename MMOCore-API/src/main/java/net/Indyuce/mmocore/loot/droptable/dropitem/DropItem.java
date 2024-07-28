package net.Indyuce.mmocore.loot.droptable.dropitem;

import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.RandomAmount;
import net.Indyuce.mmocore.loot.LootBuilder;

import java.util.Random;

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

    /// TODO make it configurable
    @Deprecated
    public static final double CHANCE_FACTOR = 7. / 100, CHANCE_POWER = 0.33333333333;

    public boolean rollChance(PlayerData player) {
        final double effectiveLuck = CHANCE_FACTOR * MMOCore.plugin.configManager.dropItemsChanceWeight * player.getStats().getStat("CHANCE");
        final double randomValue = random.nextDouble();
        return randomValue < Math.pow(chance, Math.pow(1 + effectiveLuck, CHANCE_POWER));
    }

    public abstract void collect(LootBuilder builder);
}
