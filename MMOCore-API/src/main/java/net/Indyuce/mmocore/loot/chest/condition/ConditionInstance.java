package net.Indyuce.mmocore.loot.chest.condition;

import java.util.List;
import java.util.stream.Stream;

import net.Indyuce.mmocore.MMOCore;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class ConditionInstance {
    private final Entity entity;
    private final Location applied;
    private final List<String> regions;

    public ConditionInstance(@NotNull Entity entity) {
        this(entity, entity.getLocation());
    }

    public ConditionInstance(@NotNull Entity entity, @NotNull Location applied) {
        this.entity = entity;
        this.regions = MMOCore.plugin.regionHandler.getRegions(this.applied = applied);

        regions.add("__global__");
    }

    public boolean isInRegion(String name) {
        return regions.contains(name);
    }

    @Deprecated
    public Location getAppliedLocation() {
        return applied;
    }

    @NotNull
    public Location getLocation() {
        return applied;
    }

    @NotNull
    public Entity getEntity() {
        return entity;
    }

    public Stream<String> getRegionStream() {
        return regions.stream();
    }
}
