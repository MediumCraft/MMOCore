package net.Indyuce.mmocore.comp.worldguard;

import java.util.Arrays;
import java.util.List;

import net.Indyuce.mmocore.api.droptable.condition.Condition;
import net.Indyuce.mmocore.api.droptable.condition.ConditionInstance;
import net.Indyuce.mmoitems.api.util.MMOLineConfig;

public class RegionCondition extends Condition {
	private final List<String> names;

	public RegionCondition(MMOLineConfig config) {
		super(config);

		config.validate("name");
		names = Arrays.asList(config.getString("name").split("\\,"));
	}

	@Override
	public boolean isMet(ConditionInstance entity) {
		return entity.getRegionStream().filter(str -> names.contains(str)).count() > 0;
	}
}
