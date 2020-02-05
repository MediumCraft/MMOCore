package net.Indyuce.mmocore.api.droptable.condition;

import net.Indyuce.mmocore.api.load.MMOLineConfig;
import org.bukkit.block.Biome;

import java.util.Arrays;
import java.util.List;

public class BiomeCondition extends Condition {
	private final List<String> names;

	public BiomeCondition(MMOLineConfig config) {
		super(config);

		config.validate("name");
		names = Arrays.asList(config.getString("name").split("\\,"));
	}

	@Override
	public boolean isMet(ConditionInstance entity) {
		Biome currentBiome = entity.getEntity().getLocation().getBlock().getBiome();
		return names.contains(currentBiome.name());
	}
}
