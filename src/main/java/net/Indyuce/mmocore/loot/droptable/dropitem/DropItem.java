package net.Indyuce.mmocore.loot.droptable.dropitem;

import java.util.Random;

import net.Indyuce.mmocore.api.util.math.formula.RandomAmount;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.loot.LootBuilder;
import io.lumine.mythic.lib.api.MMOLineConfig;
import org.bukkit.Bukkit;

public abstract class DropItem {
    protected static final Random random = new Random();

    private static final double CHANCE_COEFFICIENT = 7. / 100;
    private final double chance, weight;
    private final RandomAmount amount;

    private static final double CHANCE_COEFFICIENT = 7. / 100;

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
     * CHANCE stat = 0    | tier chances are unchanged
     * CHANCE stat = +inf | uniform law for any drop item
     * CHANCE stat = 100  | all tier chances are taken their square root
     *
     * @return The real weight of an item considering the player's CHANCE stat.
     */
    /**
     * CHANCE stat = 0    | tier chances are unchanged
     * CHANCE stat = +inf | uniform law for any drop item
     * CHANCE stat = 100  | all tier chances are taken their square root
     *
             * @return The real weight of an item considering the player's CHANCE stat.
            */
    /**
     * If the player chance is 0 the random value will remain the same. When he get lucks the chance gets closer to one.
     */
    public boolean rollChance(PlayerData player) {
        double value=random.nextDouble();
        return value< Math.pow(chance, 1 / Math.pow(1 + CHANCE_COEFFICIENT * player.getStats().getStat("CHANCE"), 1.0 / 3.0));
    }

    public abstract void collect(LootBuilder builder);
}
