package net.Indyuce.mmocore.api.block;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.api.block.BlockInfo.RegeneratingBlock;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.Objects;

public class SkullBlockType implements BlockType {
    private final String value;

    public SkullBlockType(MMOLineConfig config) {
        config.validate("value");

        value = config.getString("value");
    }

    public SkullBlockType(Block block) {
        value = MythicLib.plugin.getVersion().getWrapper().getSkullValue(block);
    }

    public String getValue() {
        return value;
    }

    @Override
    public void place(RegeneratingBlock block) {
        Location loc = block.getLocation();
        loc.getBlock().setType(Material.PLAYER_HEAD);

        // save skull orientation if replaced block is a player head
        if (MMOCoreUtils.isPlayerHead(block.getBlockData().getMaterial()))
            loc.getBlock().setBlockData(block.getBlockData());

        MythicLib.plugin.getVersion().getWrapper().setSkullValue(loc.getBlock(), value);
    }

    @Override
    public void regenerate(RegeneratingBlock block) {
        Location loc = block.getLocation();
        // This makes sure that if a skull loses its original rotation
        // it can revert back to it when the base block is regenerated
        loc.getBlock().setBlockData(block.getBlockData());
        MythicLib.plugin.getVersion().getWrapper().setSkullValue(loc.getBlock(), value);
    }

    @Override
    public String display() {
        return "Skull{" + value + "}";
    }

    @Override
    public boolean breakRestrictions(Block block) {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SkullBlockType that = (SkullBlockType) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
