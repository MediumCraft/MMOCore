package net.Indyuce.mmocore.api.event;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.block.BlockInfo;
import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class CustomBlockMineEvent extends PlayerDataEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private final Block block;
    private final BlockInfo info;
    private List<ItemStack> drops;

    private boolean cancelled;

    public CustomBlockMineEvent(PlayerData player, Block block, BlockInfo info, List<ItemStack> drops, boolean cancelled) {
        super(player);

        this.block = block;
        this.info = info;
        this.drops = drops;
        this.cancelled = cancelled;
    }

    public Block getBlock() {
        return block;
    }

    public List<ItemStack> getDrops() {
        return drops;
    }

    public BlockInfo getBlockInfo() {
        return info;
    }

    public void setDrops(List<ItemStack> list) {
        this.drops = list;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean value) {
        cancelled = value;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
