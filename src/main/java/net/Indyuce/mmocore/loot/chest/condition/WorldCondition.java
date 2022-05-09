package net.Indyuce.mmocore.loot.chest.condition;

import java.util.Arrays;
import java.util.List;

import io.lumine.mythic.lib.api.MMOLineConfig;

public class WorldCondition extends Condition {
	private final List<String> names;

	public WorldCondition(MMOLineConfig config) {
		super(config);

		config.validate("name");
		names = Arrays.asList(config.getString("name").split(","));
	}

	@Override
	public boolean isMet(ConditionInstance entity) {
		return names.contains(entity.getEntity().getWorld().getName()) || names.contains("__global__");
	}
}
