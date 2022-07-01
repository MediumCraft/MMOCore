package net.Indyuce.mmocore.loot;

import net.Indyuce.mmocore.api.player.PlayerData;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Random;

/**
 * Used whenever the chance stat appears in MMOCore
 *
 * @param <T> Any weighted object, currently either fishing drop
 *            items or loot chest tiers.
 */
public class RandomWeightedRoll<T extends Weighted> {
    private final Collection<T> collection;
    private final T rolled;

    private static final Random RANDOM = new Random();

    public RandomWeightedRoll(PlayerData player, Collection<T> collection, double chanceWeight) {
        this.collection = collection;

        double partialSum = 0;
        final double randomCoefficient = RANDOM.nextDouble(), chance = chanceWeight * player.getStats().getStat("CHANCE"), sum = weightedSum(chance);

        for (T item : collection) {
            partialSum += computeRealWeight(item, chance);
            if (partialSum > randomCoefficient * sum) {
                rolled = item;
                return;
            }
        }

        throw new RuntimeException("Could not roll item");
    }

    /**
     * The chance stat will make low weight items more
     * likely to be chosen by the algorithm.
     *
     * @return Randomly computed item
     */
    @NotNull
    public T rollItem() {
        return rolled;
    }

    private double weightedSum(double chance) {
        double sum = 0;
        for (T item : collection)
            sum += computeRealWeight(item, chance);
        return sum;
    }

    private static final double CHANCE_COEFFICIENT = 7. / 100;

    /**
     * chance = 0    | tier chances are unchanged
     * chance = +inf | uniform law for any drop item
     * chance = 100  | all tier chances are taken their square root
     *
     * @return The real weight of an item considering the player's chance stat.
     */
    private double computeRealWeight(T item, double chance) {
        return Math.pow(item.getWeight(), 1 / Math.pow(1 + CHANCE_COEFFICIENT * chance, 1 / 3));
    }

    /*
    Should this be used
    private double getTierCoefficient(double initialTierChance, double chance) {
    /**
     * - Chance = 0    | tier coefficient is left unchanged.
     * - Chance -> +oo | all tier coefficients are the same (1)
     * - Chance = 50   | coefficients become their square roots
     *
        return Math.pow(initialTierChance, 1 / Math.pow(1 + CHANCE_COEF * chance, 1 / 3));
    }*/
}
