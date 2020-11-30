package net.Indyuce.mmocore.comp.mythicmobs.load;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitItemStack;
import io.lumine.xikage.mythicmobs.drops.Drop;
import io.lumine.xikage.mythicmobs.drops.DropMetadata;
import io.lumine.xikage.mythicmobs.drops.IMultiDrop;
import io.lumine.xikage.mythicmobs.drops.LootBag;
import io.lumine.xikage.mythicmobs.drops.droppables.ItemDrop;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import net.Indyuce.mmocore.api.util.item.ConfigItem;
import net.Indyuce.mmocore.api.util.item.CurrencyItem;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.api.item.NBTItem;

public class GoldPouchDrop extends Drop implements IMultiDrop {
	private final int min;
	private final int max;

	private static final Random random = new Random();

	public GoldPouchDrop(MythicLineConfig config) {
		super(config.getLine(), config);

		min = config.getInteger("min", 1);
		max = config.getInteger("max", 10);
	}

	@SuppressWarnings("deprecation")
	@Override
	public LootBag get(DropMetadata metadata) {
		LootBag loot = new LootBag(metadata);
		NBTItem nbt = NBTItem.get(new ConfigItem("MOB_GOLD_POUCH").build());

		ItemStack[] content = new ItemStack[18];
		int money = random.nextInt(max - min + 1) + min;

		for (int j = 0; j < 7 && money > 0; j++) {
			int a = j == 6 ? money : Math.max(1, (int) ((.12 + random.nextDouble() * .4) * (double) money));
			money -= a;

			if (a < 30 && random.nextDouble() < .3) {
				content[getAvailableSlot(content)] = new CurrencyItem("GOLD_COIN", 1, a).build();
				continue;
			}

			content[getAvailableSlot(content)] = new CurrencyItem("NOTE", a, 1).build();
		}

		nbt.addTag(new ItemTag("RpgPouchSize", 18), new ItemTag("RpgPouchMob", true), new ItemTag("RpgPouchInventory", MMOCoreUtils.toBase64(content)));
		loot.add(new ItemDrop(this.getLine(), (MythicLineConfig) this.getConfig(), new BukkitItemStack(nbt.toItem())));
		return loot;
	}

	private int getAvailableSlot(ItemStack[] content) {
		int slot;
		while (content[slot = random.nextInt(content.length)] != null)
			if(content[slot].getType() == Material.AIR) break;
		return slot;
	}
}
