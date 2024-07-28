package net.Indyuce.mmocore.loot.chest;

import io.lumine.mythic.lib.api.math.ScalingFormula;
import io.lumine.mythic.lib.util.annotation.BackwardsCompatibility;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.loot.Weighted;
import net.Indyuce.mmocore.loot.droptable.DropTable;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ChestTier implements Weighted {
    private final TierEffect effect;
    // TODO Capacity should be inherent to drop table
    // TODO make capacity any numeric formula, parsed with a player
    @BackwardsCompatibility(version = "1.12.1")
    @Nullable
    private final ScalingFormula capacity;
    private final DropTable table;
    private final double chance;

    public ChestTier(ConfigurationSection config) {
        effect = config.isConfigurationSection("effect") ? new TierEffect(config.getConfigurationSection("effect")) : null;
        capacity = config.contains("capacity") ? new ScalingFormula(config.get("capacity")) : null;
        chance = config.getDouble("chance");
        table = MMOCore.plugin.dropTableManager.loadDropTable(config.get("drops"));
    }

    public double rollCapacity(@NotNull PlayerData player) {
        return capacity == null ? table.getCapacity() : capacity.calculate(player.getLevel());
    }

    public double getChance() {
        return chance;
    }

    @Override
    public double getWeight() {
        return chance;
    }

    @NotNull
    public DropTable getDropTable() {
        return table;
    }

    public boolean hasEffect() {
        return effect != null;
    }

    public TierEffect getEffect() {
        return effect;
    }
}
