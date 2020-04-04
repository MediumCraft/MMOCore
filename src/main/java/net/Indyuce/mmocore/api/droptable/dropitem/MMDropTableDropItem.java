package net.Indyuce.mmocore.api.droptable.dropitem;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Level;

import org.bukkit.inventory.ItemStack;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.drops.Drop;
import io.lumine.xikage.mythicmobs.drops.DropMetadata;
import io.lumine.xikage.mythicmobs.drops.DropTable;
import io.lumine.xikage.mythicmobs.drops.IItemDrop;
import io.lumine.xikage.mythicmobs.drops.LootBag;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmoitems.api.util.MMOLineConfig;

public class MMDropTableDropItem extends DropItem {
	private DropTable dropTable;
	private DropMetadata metadata = new DropMetadata(null, null);

	public MMDropTableDropItem(MMOLineConfig config) {
		super(config);

		config.validate("id");
		String id = config.getString("id");

		try {
			dropTable = MythicMobs.inst().getDropManager().getDropTable(id).get();
		} catch(NoSuchElementException e) {
			MMOCore.log(Level.WARNING, "Could not find MM drop table" + id);
		}
	}

	@Override
	public void collect(List<ItemStack> total) {
		LootBag lootBag = dropTable.generate(metadata);
		
		for(Drop type : lootBag.getDrops()) {
			if(type instanceof IItemDrop) {
				total.add(BukkitAdapter.adapt(((IItemDrop)type).getDrop(metadata)));
			}
		}
	}
}
