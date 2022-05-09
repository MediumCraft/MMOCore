package net.Indyuce.mmocore.comp.region;

import net.Indyuce.mmocore.api.condition.Condition;
import net.Indyuce.mmocore.api.load.MMOLoader;
import io.lumine.mythic.lib.api.MMOLineConfig;

public class WorldGuardMMOLoader extends MMOLoader {

	@Override
	public Condition loadCondition(MMOLineConfig config) {

		if (config.getKey().equals("region"))
			return new RegionCondition(config);

		return null;
	}
}
