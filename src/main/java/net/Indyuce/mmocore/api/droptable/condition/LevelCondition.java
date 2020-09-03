package net.Indyuce.mmocore.api.droptable.condition;

import net.mmogroup.mmolib.api.MMOLineConfig;
import net.mmogroup.mmolib.api.player.MMOPlayerData;
import org.bukkit.entity.Player;

public class LevelCondition extends Condition {
    private final int amount;

    private final String profession;

    public LevelCondition(MMOLineConfig config) {
        super(config);

        config.validate("amount");

        amount = config.getInt("amount");
        profession = config.contains("profession") ? config.getString("profession") : null;
    }

    @Override
    public boolean isMet(ConditionInstance entity) {
        if (entity.getEntity() instanceof Player) {
            int level = (profession != null) ? MMOPlayerData.get((Player) entity.getEntity()).getMMOCore().getCollectionSkills().getLevel(profession)
                    : MMOPlayerData.get((Player) entity.getEntity()).getMMOCore().getLevel();
            return level >= amount;
        }
        return false;
    }
}
