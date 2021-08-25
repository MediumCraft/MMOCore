package net.Indyuce.mmocore.comp.region;

import java.util.Arrays;
import java.util.List;

import net.Indyuce.mmocore.loot.droptable.condition.Condition;
import net.Indyuce.mmocore.loot.droptable.condition.ConditionInstance;
import io.lumine.mythic.lib.api.MMOLineConfig;

public class RegionCondition extends Condition {
	private final List<String> names;

	public RegionCondition(MMOLineConfig config) {
		super(config);

		config.validate("name");
		names = Arrays.asList(config.getString("name").split(","));
	}

	@Override
	public boolean isMet(ConditionInstance entity) {
		return entity.getRegionStream().anyMatch(names::contains);
	}
}
