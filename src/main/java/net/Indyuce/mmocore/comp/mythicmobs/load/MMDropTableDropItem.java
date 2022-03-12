package net.Indyuce.mmocore.comp.mythicmobs.load;

import io.lumine.mythic.api.drops.DropMetadata;
import io.lumine.mythic.api.drops.IItemDrop;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.drops.Drop;
import io.lumine.mythic.core.drops.DropMetadataImpl;
import io.lumine.mythic.core.drops.DropTable;
import io.lumine.mythic.core.drops.LootBag;
import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.loot.LootBuilder;
import net.Indyuce.mmocore.loot.droptable.dropitem.DropItem;

import java.util.NoSuchElementException;

public class MMDropTableDropItem extends DropItem {
    private final DropTable dropTable;

    private static final DropMetadata DROP_METADATA = new DropMetadataImpl(null, null);

    public MMDropTableDropItem(MMOLineConfig config) {
        super(config);

        config.validate("id");
        String id = config.getString("id");

        try {
            dropTable = MythicBukkit.inst().getDropManager().getDropTable(id).orElse(null);
        } catch (NoSuchElementException exception) {
            throw new IllegalArgumentException("Could not find MM drop table with ID '" + id + "'");
        }
    }

    @Override
    public void collect(LootBuilder builder) {
        LootBag lootBag = dropTable.generate(DROP_METADATA);
        for (Drop type : lootBag.getDrops())
            if (type instanceof IItemDrop)
                builder.addLoot(BukkitAdapter.adapt(((IItemDrop) type).getDrop(DROP_METADATA, 1)));
    }
}
