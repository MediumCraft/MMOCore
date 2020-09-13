package net.Indyuce.mmocore.listener;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.block.BlockInfo;
import net.Indyuce.mmocore.api.block.VanillaBlockType;
import net.Indyuce.mmocore.api.event.CustomBlockMineEvent;
import net.Indyuce.mmocore.api.player.PlayerData;

public class BlockListener implements Listener {
	private static final BlockFace[] order = { BlockFace.UP, BlockFace.DOWN, BlockFace.EAST, BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH };

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onCustomBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		if (player.getGameMode() == GameMode.CREATIVE)
			return;

		String savedData = event.getBlock().getBlockData().getAsString();
		Block block = event.getBlock();

		/*
		 * if custom mining enabled, check for item breaking restrictions
		 */
		boolean customMine = MMOCore.plugin.mineManager.isEnabled(player, block.getLocation());
		if (!customMine)
			return;

		BlockInfo info = MMOCore.plugin.mineManager.getInfo(block);
		if (info == null || !info.getBlock().breakRestrictions(block)) {
			event.setCancelled(true);
			return;
		}

		/*
		 * calls the event and listen for cancel & for drops changes... also
		 * allows to apply tool durability & enchants to drops, etc.
		 */
		CustomBlockMineEvent called = new CustomBlockMineEvent(PlayerData.get(player), block, info);
		Bukkit.getPluginManager().callEvent(called);
		if (called.isCancelled()) {
			event.setCancelled(true);
			return;
		}

		ItemStack item = player.getInventory().getItemInMainHand();
		if (!MMOCore.plugin.restrictionManager.checkPermissions(item, info.getBlock())) {
			MMOCore.plugin.configManager.getSimpleMessage("cannot-break").send(player);
			event.setCancelled(true);
			return;
		}

		/*
		 * remove vanilla drops if needed
		 */
		if (!info.hasVanillaDrops()) {
			// event.setDropItems(false); // May not work
			event.setCancelled(true);
			event.getBlock().setType(Material.AIR);
		}

		/*
		 * apply triggers, add experience info to the event so the other events
		 * can give exp to other TOOLS and display HOLOGRAMS
		 */
		if (info.hasTriggers() && !block.hasMetadata("player_placed")) {
			PlayerData playerData = PlayerData.get(player);
			info.getTriggers().forEach(trigger -> trigger.apply(playerData));
			/**
			 * if (!block.hasMetadata("player_placed") && info.hasExperience()
			 * && MMOCore.plugin.hasHolograms())
			 * MMOCore.plugin.hologramSupport.displayIndicator(block.getLocation().add(.5,
			 * 1.5, .5),
			 * MMOCore.plugin.configManager.getSimpleMessage("exp-hologram",
			 * "exp", "" + called.getGainedExperience().getValue()).message(),
			 * player);
			 */
		}

		/*
		 * apply drop tables
		 */
		if (info.hasDropTable()) {
			Location dropLocation = getSafeDropLocation(block,
					!block.getType().isSolid() || !(info.regenerates() && info.getRegenerationInfo().hasTemporaryBlock()));
			for (ItemStack drop : called.getDrops())
				if (drop.getType() != Material.AIR && drop.getAmount() > 0)
					block.getWorld().dropItemNaturally(dropLocation, drop);
		}

		/*
		 * enable block regen.
		 */
		if (info.hasRegen())
			MMOCore.plugin.mineManager.initialize(info.startRegeneration(Bukkit.createBlockData(savedData), block.getLocation()));
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void registerPlayerPlacedBlocksTag(BlockPlaceEvent event) {
		event.getBlock().setMetadata("player_placed", new FixedMetadataValue(MMOCore.plugin, true));
	}

	@EventHandler
	public void blockPistonExtend(BlockPistonExtendEvent event) {
		Block movedBlock = event.getBlock();
		if (!movedBlock.hasMetadata("player_placed"))
			return;
		BlockFace direction = event.getDirection();
		movedBlock = movedBlock.getRelative(direction, 2);

		for (Block b : event.getBlocks())
			if (b.hasMetadata("player_placed")) {
				movedBlock = b.getRelative(direction);
				movedBlock.setMetadata("player_placed", new FixedMetadataValue(MMOCore.plugin, true));
			}
	}

	@EventHandler
	public void blockPistonRetract(BlockPistonRetractEvent event) {
		BlockFace direction = event.getDirection();
		Block movedBlock = event.getBlock().getRelative(direction);
		movedBlock.setMetadata("player_placed", new FixedMetadataValue(MMOCore.plugin, true));

		for (Block block : event.getBlocks()) {
			movedBlock = block.getRelative(direction);
			movedBlock.setMetadata("player_placed", new FixedMetadataValue(MMOCore.plugin, true));
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void handlePlayerStatistics(CustomBlockMineEvent event) {
		if (event.getBlockInfo().getBlock() instanceof VanillaBlockType)
			event.getPlayer().incrementStatistic(Statistic.MINE_BLOCK, ((VanillaBlockType) event.getBlockInfo().getBlock()).getType());
	}

	private Location getSafeDropLocation(Block block, boolean self) {
		if (block.getType() == Material.AIR && self)
			return block.getLocation();

		for (BlockFace face : order) {
			Block relative = block.getRelative(face);
			if (!relative.getType().isSolid())
				return relative.getLocation().add(block.getLocation().subtract(relative.getLocation()).multiply(.6));
		}

		return block.getLocation();
	}
}
