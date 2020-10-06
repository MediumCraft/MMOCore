package net.Indyuce.mmocore.api.droptable.dropitem;

import org.apache.commons.lang.Validate;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.droptable.DropTable;
import net.Indyuce.mmocore.api.droptable.condition.ConditionInstance;
import net.Indyuce.mmocore.api.loot.LootBuilder;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.mmogroup.mmolib.api.MMOLineConfig;

public class DropTableDropItem extends DropItem {
	private final DropTable dropTable;

	public DropTableDropItem(MMOLineConfig config) {
		super(config);

		config.validate("id");
		String id = config.getString("id");

		Validate.isTrue(MMOCore.plugin.dropTableManager.has(id), "Could not find drop table " + id);
		this.dropTable = MMOCore.plugin.dropTableManager.get(id);
	}

	@Override
	public void collect(LootBuilder builder) {
		PlayerData data = builder.getEntity();
		if(!data.isOnline()) return;
		if (dropTable.areConditionsMet(new ConditionInstance(data.getPlayer())))
			for (int j = 0; j < rollAmount(); j++)
				builder.addLoot(dropTable.collect(builder));
	}
}
