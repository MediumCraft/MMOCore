package net.Indyuce.mmocore.loot.chest;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.util.configobject.ConfigSectionObject;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.loot.droptable.DropTable;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

public class StaticLootChest {
    private final String id;
    private final Location location;
    private final DropTable table;

    @Nullable
    private String permission;

    public StaticLootChest(ConfigurationSection config) {
        this.id = config.getName();
        this.location = UtilityMethods.readLocation(new ConfigSectionObject(config.getConfigurationSection("location")));
        this.table = MMOCore.plugin.dropTableManager.loadDropTable(config.get("drops"));

    }

    public String getId() {
        return id;
    }

    public Location getLocation() {
        return location;
    }

    public DropTable getTable() {
        return table;
    }
}
