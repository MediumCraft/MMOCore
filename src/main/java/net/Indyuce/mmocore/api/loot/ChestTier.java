package net.Indyuce.mmocore.api.loot;

import org.bukkit.configuration.ConfigurationSection;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.droptable.DropTable;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.mmogroup.mmolib.api.math.ScalingFormula;

public class ChestTier {
	private final TierEffect effect;
	private final ScalingFormula capacity;
	private final DropTable table;

	public final double chance;

	public ChestTier(ConfigurationSection config) {
		effect = config.isConfigurationSection("effect") ? new TierEffect(config.getConfigurationSection("effect")) : null;
		capacity = new ScalingFormula(config.get("capacity"));
		chance = config.getDouble("chance");
		table = MMOCore.plugin.dropTableManager.loadDropTable(config.get("drops"));
	}

	public double rollCapacity(PlayerData player) {
		return capacity.calculate(player.getLevel());
	}

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
