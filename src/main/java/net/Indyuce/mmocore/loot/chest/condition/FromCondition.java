package net.Indyuce.mmocore.loot.chest.condition;

import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigFile;
import org.apache.commons.lang.Validate;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class FromCondition extends Condition {
    private final List<Condition> conditions = new ArrayList<>();


    public FromCondition(MMOLineConfig config) {
        super(config);
        List<String> list = new ConfigFile("conditions")
                .getConfig().getStringList(config.getString("source"));
        Validate.isTrue(list.size() != 0, "There is no source matching " + config.getString("key"));
        list.stream()
                .map(MMOLineConfig::new)
                .forEach(mmoLineConfig ->
                        conditions.add(MMOCore.plugin.loadManager.loadCondition(mmoLineConfig)));
    }


    @Override
    public boolean isMet(ConditionInstance entity) {
        return conditions.stream().allMatch(condition -> condition.isMet(entity));
    }
}
