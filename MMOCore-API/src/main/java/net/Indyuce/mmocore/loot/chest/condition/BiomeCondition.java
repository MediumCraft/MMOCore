package net.Indyuce.mmocore.loot.chest.condition;

import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.version.VersionUtils;

import java.util.Arrays;
import java.util.List;

public class BiomeCondition extends Condition {
    private final List<String> names;

    public BiomeCondition(MMOLineConfig config) {
        super(config);

        config.validate("name");
        names = Arrays.asList(config.getString("name").toUpperCase().split(","));
    }

    @Override
    public boolean isMet(ConditionInstance instance) {
        return names.contains(VersionUtils.name(instance.getLocation().getBlock().getBiome()));
    }
}
