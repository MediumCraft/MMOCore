package net.Indyuce.mmocore.comp.mythicmobs.load;

import io.lumine.mythic.api.adapters.AbstractItemStack;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.drops.DropMetadata;
import io.lumine.mythic.api.drops.IItemDrop;
import io.lumine.mythic.bukkit.adapters.BukkitItemStack;
import io.lumine.mythic.core.drops.Drop;
import net.Indyuce.mmocore.util.item.CurrencyItemBuilder;

import java.util.Random;

public class CurrencyItemDrop extends Drop implements IItemDrop {
    private final String key;
    private final int minw;
    private final int maxw;

    private static final Random random = new Random();

    public CurrencyItemDrop(String key, MythicLineConfig config) {
        super(config.getLine(), config);

        this.key = key;
        minw = config.getInteger("minw", 1);
        maxw = config.getInteger("maxw", 1);
    }

    @Override
    public AbstractItemStack getDrop(DropMetadata dropMetadata, double v) {
        return new BukkitItemStack(new CurrencyItemBuilder(key, random(minw, maxw)).build());
    }

    private int random(int a, int b) {
        return random.nextInt(b - a + 1) + a;
    }
}
