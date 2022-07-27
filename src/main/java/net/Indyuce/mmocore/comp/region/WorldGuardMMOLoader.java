package net.Indyuce.mmocore.comp.region;

import net.Indyuce.mmocore.loot.chest.condition.Condition;
import net.Indyuce.mmocore.api.load.MMOLoader;
import io.lumine.mythic.lib.api.MMOLineConfig;

import java.util.Arrays;
import java.util.List;

public class WorldGuardMMOLoader extends MMOLoader {

	@Override
	public List<Condition> loadCondition(MMOLineConfig config) {

		if (config.getKey().equals("region"))
			return Arrays.asList(new RegionCondition(config));

		return null;
	}
}
