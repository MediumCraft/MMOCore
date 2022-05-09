package net.Indyuce.mmocore.loot.condition;

import io.lumine.mythic.lib.api.MMOLineConfig;
import org.bukkit.entity.Player;

public class PermissionCondition extends Condition {
    private final String perm;

    public PermissionCondition(MMOLineConfig config) {
        super(config);

        config.validate("node");
        perm = config.getString("node");
    }

    @Override
    public boolean isMet(ConditionInstance entity) {
        if (entity.getEntity() instanceof Player)
            return entity.getEntity().hasPermission(perm);
        return false;
    }
}
