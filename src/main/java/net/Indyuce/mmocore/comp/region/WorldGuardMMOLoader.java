package net.Indyuce.mmocore.comp.region;

import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.api.load.MMOLoader;
import net.Indyuce.mmocore.loot.chest.condition.Condition;

public class WorldGuardMMOLoader extends MMOLoader {

    @Override
    public Condition loadCondition(MMOLineConfig config) {

        if (config.getKey().equals("region"))
            return new RegionCondition(config);

        return null;
    }
}
