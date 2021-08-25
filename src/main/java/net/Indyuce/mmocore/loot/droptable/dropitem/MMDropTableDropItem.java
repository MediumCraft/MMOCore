package net.Indyuce.mmocore.loot.droptable.dropitem;

import java.util.NoSuchElementException;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.drops.Drop;
import io.lumine.xikage.mythicmobs.drops.DropMetadata;
import io.lumine.xikage.mythicmobs.drops.DropTable;
import io.lumine.xikage.mythicmobs.drops.IItemDrop;
import io.lumine.xikage.mythicmobs.drops.LootBag;
import net.Indyuce.mmocore.loot.LootBuilder;
import io.lumine.mythic.lib.api.MMOLineConfig;

public class MMDropTableDropItem extends DropItem {
	private final DropTable dropTable;

	private static final DropMetadata metadata = new DropMetadata(null, null);

	public MMDropTableDropItem(MMOLineConfig config) {
		super(config);

		config.validate("id");
		String id = config.getString("id");

		try {
			dropTable = MythicMobs.inst().getDropManager().getDropTable(id).orElse(null);
		} catch (NoSuchElementException exception) {
			throw new IllegalArgumentException("Could not find MM drop table with ID '" + id + "'");
		}
	}

	@Override
	public void collect(LootBuilder builder) {
		LootBag lootBag = dropTable.generate(metadata);
		for (Drop type : lootBag.getDrops())
			if (type instanceof IItemDrop)
				builder.addLoot(BukkitAdapter.adapt(((IItemDrop) type).getDrop(metadata)));
	}
}
