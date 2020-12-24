package net.Indyuce.mmocore.api.event;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmocore.api.block.BlockInfo;
import net.Indyuce.mmocore.api.droptable.condition.ConditionInstance;
import net.Indyuce.mmocore.api.experience.ExperienceInfo;
import net.Indyuce.mmocore.api.loot.LootBuilder;
import net.Indyuce.mmocore.api.player.PlayerData;

public class CustomBlockMineEvent extends PlayerDataEvent implements Cancellable {
	private static final HandlerList handlers = new HandlerList();

	private final Block block;
	private final BlockInfo info;
	private final List<ItemStack> drops;
	private final ExperienceInfo experience;

	@Deprecated
	private boolean canBreak;
	private boolean cancelled = false;

	public CustomBlockMineEvent(PlayerData player, Block block, BlockInfo info, boolean canBreak) {
		super(player);

		this.block = block;
		this.info = info;
		this.drops = (info.hasDropTable() && player.isOnline() && info.getDropTable().areConditionsMet(new ConditionInstance(player.getPlayer())))
				? info.collectDrops(new LootBuilder(player, 0))
				: new ArrayList<>();
		this.experience = info.hasExperience() ? info.getExperience().newInfo() : null;
		this.canBreak = canBreak;
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

	public boolean hasGainedExperience() {
		return experience != null;
	}

	public ExperienceInfo getGainedExperience() {
		return experience;
	}

	@Deprecated
	public boolean canBreak() {
		return canBreak;
	}

	@Deprecated
	public void setCanBreak(boolean value) {
		canBreak = value;
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
