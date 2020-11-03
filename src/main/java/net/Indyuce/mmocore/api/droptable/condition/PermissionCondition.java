package net.Indyuce.mmocore.api.droptable.condition;

import org.bukkit.entity.Player;

import net.mmogroup.mmolib.api.MMOLineConfig;

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
        	return ((Player) entity).hasPermission(perm);
        return false;
    }
}
