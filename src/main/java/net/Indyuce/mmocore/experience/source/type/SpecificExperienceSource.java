package net.Indyuce.mmocore.experience.source.type;

import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.api.util.math.formula.RandomAmount;
import net.Indyuce.mmocore.experience.EXPSource;
import net.Indyuce.mmocore.experience.dispenser.ExperienceDispenser;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

public abstract class SpecificExperienceSource<T> extends ExperienceSource<T> {
    private final RandomAmount amount;
    double counter = 0;

    /**
     * Used to register experience sources with SPECIFIC experience outputs.
     * Other experience sources like ENCHANT have their exp output depend on the
     * enchanted item. ALCHEMY exp outputs depend on the potion crafted
     */
    public SpecificExperienceSource(ExperienceDispenser dispenser, MMOLineConfig config) {
        super(dispenser);

        config.validate("amount");
        amount = new RandomAmount(config.getString("amount"));
    }

    public RandomAmount getAmount() {
        return amount;
    }

    public double rollAmount() {
        return amount.calculate();
    }

    /**
     * Used when a player needs to gain experience after performing the action
     * corresponding to this exp source
     *
     * @param player           Player gaining the exp
     * @param multiplier       Used by the CraftItem experience source, multiplies the exp
     *                         earned by a certain factor. When crafting an item, the
     *                         multiplier is equal to the amount of items crafted
     * @param hologramLocation Location used to display the exp hologram
     */
    public void giveExperience(PlayerData player, double multiplier, @Nullable Location hologramLocation) {
        getDispenser().giveExperience(player, rollAmount() * multiplier, hologramLocation, EXPSource.SOURCE);
    }
}
