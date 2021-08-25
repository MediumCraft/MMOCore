package net.Indyuce.mmocore.loot.droptable.condition;

import net.Indyuce.mmocore.api.player.PlayerData;
import io.lumine.mythic.lib.api.MMOLineConfig;
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
			int level = (profession != null) ? PlayerData.get(entity.getEntity().getUniqueId()).getCollectionSkills().getLevel(profession) : PlayerData.get(entity.getEntity().getUniqueId()).getLevel();
			return level >= amount;
		}
		return false;
	}
}
