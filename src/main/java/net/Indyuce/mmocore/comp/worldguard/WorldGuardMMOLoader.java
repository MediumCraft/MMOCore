package net.Indyuce.mmocore.comp.worldguard;

import net.Indyuce.mmocore.api.droptable.condition.Condition;
import net.Indyuce.mmocore.api.load.MMOLoader;
import net.Indyuce.mmoitems.api.util.MMOLineConfig;

public class WorldGuardMMOLoader extends MMOLoader {

	@Override
	public Condition loadCondition(MMOLineConfig config) {

		if (config.getKey().equals("region"))
			return new RegionCondition(config);

		return null;
	}
}
