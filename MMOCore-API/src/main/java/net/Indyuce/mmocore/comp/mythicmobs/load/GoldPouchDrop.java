package net.Indyuce.mmocore.comp.mythicmobs.load;

import io.lumine.mythic.api.adapters.AbstractItemStack;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.drops.DropMetadata;
import io.lumine.mythic.api.drops.IItemDrop;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.core.drops.Drop;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import net.Indyuce.mmocore.util.item.CurrencyItemBuilder;
import net.Indyuce.mmocore.util.item.SimpleItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class GoldPouchDrop extends Drop implements IItemDrop {
    private final int min;
    private final int max;

    private static final Random random = new Random();

    public GoldPouchDrop(MythicLineConfig config) {
        super(config.getLine(), config);

        min = config.getInteger("min", 1);
        max = config.getInteger("max", 10);
    }

    @Override
    public AbstractItemStack getDrop(DropMetadata dropMetadata, double v) {
        NBTItem nbt = NBTItem.get(new SimpleItemBuilder("MOB_GOLD_POUCH").build());

        ItemStack[] content = new ItemStack[18];
        int money = random.nextInt(max - min + 1) + min;

        for (int j = 0; j < 7 && money > 0; j++) {
            int a = j == 6 ? money : Math.max(1, (int) ((.12 + random.nextDouble() * .4) * (double) money));
            money -= a;

            if (a < 30 && random.nextDouble() < .3) {
                content[getAvailableSlot(content)] = setAmount(new CurrencyItemBuilder("GOLD_COIN", 1).build(), a);
                continue;
            }

            content[getAvailableSlot(content)] = new CurrencyItemBuilder("NOTE", a).build();
        }

        nbt.addTag(new ItemTag("RpgPouchSize", 18), new ItemTag("RpgPouchMob", true), new ItemTag("RpgPouchInventory", MMOCoreUtils.toBase64(content)));
        // Not great wrt to performance. Should build the item like MM does
        return BukkitAdapter.adapt(nbt.toItem());
    }

    private ItemStack setAmount(ItemStack item, int amount) {
        item.setAmount(amount);
        return item;
    }

    private int getAvailableSlot(ItemStack[] content) {
        int slot;
        while (content[slot = random.nextInt(content.length)] != null)
            if (content[slot].getType() == Material.AIR) break;
        return slot;
    }
}
