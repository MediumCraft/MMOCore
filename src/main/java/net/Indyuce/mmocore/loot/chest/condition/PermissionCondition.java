package net.Indyuce.mmocore.loot.chest.condition;

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
        return entity.getEntity() instanceof Player && entity.getEntity().hasPermission(perm);
    }
}
