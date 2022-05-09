package net.Indyuce.mmocore.api.condition;

import java.util.Arrays;
import java.util.List;

import org.bukkit.block.Biome;

import io.lumine.mythic.lib.api.MMOLineConfig;

public class BiomeCondition extends Condition {
	private final List<String> names;

	public BiomeCondition(MMOLineConfig config) {
		super(config);

		config.validate("name");
		names = Arrays.asList(config.getString("name").toUpperCase().split(","));
	}

	@Override
	public boolean isMet(ConditionInstance entity) {
		Biome currentBiome = entity.getEntity().getLocation().getBlock().getBiome();
		return names.contains(currentBiome.name());
	}
}
