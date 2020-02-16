package net.Indyuce.mmocore.comp.mythicmobs.load;

import java.util.Random;

import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitItemStack;
import io.lumine.xikage.mythicmobs.drops.Drop;
import io.lumine.xikage.mythicmobs.drops.DropMetadata;
import io.lumine.xikage.mythicmobs.drops.IMultiDrop;
import io.lumine.xikage.mythicmobs.drops.LootBag;
import io.lumine.xikage.mythicmobs.drops.droppables.ItemDrop;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import net.Indyuce.mmocore.api.util.item.CurrencyItem;

public class CurrencyItemDrop extends Drop implements IMultiDrop {
	private String key;
	private int minw, maxw;

	private static final Random random = new Random();

	public CurrencyItemDrop(String key, MythicLineConfig config) {
		super(config.getLine(), config);
		
		this.key = key;
		minw = config.getInteger("minw", 1);
		maxw = config.getInteger("maxw", 1);
	}

	@SuppressWarnings("deprecation")
	@Override
	public LootBag get(DropMetadata metadata) {
		LootBag loot = new LootBag(metadata);
		loot.add(new ItemDrop(this.getLine(), (MythicLineConfig) this.getConfig(), new BukkitItemStack(new CurrencyItem(key, random(minw, maxw)).build())));
		return loot;
	}

	private int random(int a, int b) {
		return random.nextInt(b - a + 1) + a;
	}
}
