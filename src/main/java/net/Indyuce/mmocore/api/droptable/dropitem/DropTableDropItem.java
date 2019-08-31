package net.Indyuce.mmocore.api.droptable.dropitem;

import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.droptable.DropTable;
import net.Indyuce.mmocore.api.load.MMOLineConfig;

public class DropTableDropItem extends DropItem {
	private DropTable dropTable;

	public DropTableDropItem(MMOLineConfig config) {
		super(config);

		config.validate("id");
		String id = config.getString("id");

		Validate.isTrue(MMOCore.plugin.dropTableManager.has(id), "Could not find drop table " + id);
		this.dropTable = MMOCore.plugin.dropTableManager.get(id);
	}

	@Override
	public void collect(List<ItemStack> total) {
		for (int j = 0; j < rollAmount(); j++)
			total.addAll(dropTable.collect());
	}
}
