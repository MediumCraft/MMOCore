package net.Indyuce.mmocore.util.item;

import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.waypoint.Waypoint;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class WaypointBookBuilder extends AbstractItemBuilder {
    private final Waypoint waypoint;

    public WaypointBookBuilder(Waypoint waypoint) {
        super(MMOCore.plugin.configItems.get("WAYPOINT_BOOK"));

        this.waypoint = waypoint;

        addPlaceholders("waypoint", waypoint.getName());
    }

    @Override
    public void whenBuildingMeta(ItemStack item, ItemMeta meta) {

    }

    @Override
    public void whenBuildingNBT(NBTItem nbtItem) {
        nbtItem.addTag(new ItemTag("WaypointBookId", waypoint.getId()));
    }
}
