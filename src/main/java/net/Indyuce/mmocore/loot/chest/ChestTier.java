package net.Indyuce.mmocore.loot.chest;

import io.lumine.mythic.lib.api.math.ScalingFormula;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.loot.droptable.DropTable;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.configuration.ConfigurationSection;

public class ChestTier {
	private final TierEffect effect;
	private final ScalingFormula capacity;
	private final DropTable table;
	private final double chance;

	public ChestTier(ConfigurationSection config) {
		effect = config.isConfigurationSection("effect") ? new TierEffect(config.getConfigurationSection("effect")) : null;
		capacity = new ScalingFormula(config.get("capacity"));
		chance = config.getDouble("chance");
		table = MMOCore.plugin.dropTableManager.loadDropTable(config.get("drops"));
	}

	public double rollCapacity(PlayerData player) {
		return capacity.calculate(player.getLevel());
	}

	public double getChance() {
		return chance;
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
