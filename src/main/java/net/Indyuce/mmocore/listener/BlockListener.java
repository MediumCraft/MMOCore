package net.Indyuce.mmocore.listener;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
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
import net.Indyuce.mmocore.api.event.CustomBlockMineEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.manager.CustomBlockManager.BlockInfo;
import net.Indyuce.mmocore.manager.RestrictionManager.BlockPermissions;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.CustomBlock;
import net.mmogroup.mmolib.MMOLib;

public class BlockListener implements Listener {
	private static final BlockFace[] order = { BlockFace.UP, BlockFace.DOWN, BlockFace.EAST, BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH };

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void a(BlockBreakEvent event) {
		Player player = event.getPlayer();
		if (player.getGameMode() == GameMode.CREATIVE)
			return;

		String savedData = MMOLib.plugin.getVersion().isStrictlyHigher(1, 12) ? event.getBlock().getBlockData().getAsString() : "";
		Block block = event.getBlock();
		/*
		 * if custom mining enabled, check for item breaking restrictions
		 */
		boolean customMine = MMOCore.plugin.mineManager.isEnabled(player, block.getLocation());
		ItemStack item = player.getInventory().getItemInMainHand();

		if (customMine) {

			BlockInfo info = MMOCore.plugin.mineManager.getInfo(block);
			if (info == null) {
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

			BlockPermissions perms = MMOCore.plugin.restrictionManager.getPermissions(item.getType());
			if (perms == null) {
				event.setCancelled(true);
				return;
			}

			if (!perms.canMine(getBlockName(block))) {
				MMOCore.plugin.configManager.getSimpleMessage("cannot-break").send(player);
				event.setCancelled(true);
				return;
			}

			/*
			 * remove vanilla drops if needed
			 */
			if (!info.hasVanillaDrops()) {
				event.setDropItems(false); // May not work
				// event.setCancelled(true);
				event.getBlock().setType(Material.AIR);
			}

			/*
			 * apply triggers, add experience info to the event so the other
			 * events can give exp to other TOOLS and display HOLOGRAMS
			 */
			if (info.hasTriggers() && !block.hasMetadata("player_placed")) {
				PlayerData playerData = PlayerData.get(player);
				info.getTriggers().forEach(trigger -> trigger.apply(playerData));
				/**
				 * if (!block.hasMetadata("player_placed") &&
				 * info.hasExperience() && MMOCore.plugin.hasHolograms())
				 * MMOCore.plugin.hologramSupport.displayIndicator(block.getLocation().add(.5,
				 * 1.5, .5),
				 * MMOCore.plugin.configManager.getSimpleMessage("exp-hologram",
				 * "exp", "" +
				 * called.getGainedExperience().getValue()).message(), player);
				 */
			}

			/*
			 * apply drop tables
			 */
			if (info.hasDropTable()) {
				Location dropLocation = getSafeDropLocation(block, true);
				for (ItemStack drop : called.getDrops())
					if (drop.getType() != Material.AIR && drop.getAmount() > 0)
						block.getWorld().dropItemNaturally(dropLocation, drop);
			}

			/*
			 * enable block regen.
			 */
			if (info.hasRegen()) {
				if (MMOLib.plugin.getVersion().isStrictlyHigher(1, 12))
					MMOCore.plugin.mineManager.initialize(info.generateRegenInfo(Bukkit.createBlockData(savedData), block.getLocation()));
				else
					MMOCore.plugin.mineManager.initialize(info.generateRegenInfo(block.getLocation()));
			}
		}
	}

	private String getBlockName(Block block) {
		if (MMOCore.plugin.isMILoaded())
			if (MMOItems.plugin.getCustomBlocks().isMushroomBlock(block.getType())) {
				CustomBlock cblock = CustomBlock.getFromData(block.getBlockData());
				if (cblock != null)
					return "MICUSTOM_" + cblock.getId();
			}

		return block.getType().toString();
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void b(BlockPlaceEvent event) {
		event.getBlock().setMetadata("player_placed", new FixedMetadataValue(MMOCore.plugin, true));
	}

	@EventHandler
	public void c1(BlockPistonExtendEvent event) {
		Block movedBlock = event.getBlock();
		if (!movedBlock.hasMetadata("player_placed"))
			return;
		BlockFace direction = event.getDirection();
		movedBlock = movedBlock.getRelative(direction, 2);

		for (Block b : event.getBlocks()) {
			if (b.hasMetadata("player_placed")) {
				movedBlock = b.getRelative(direction);
				movedBlock.setMetadata("player_placed", new FixedMetadataValue(MMOCore.plugin, true));
			}
		}
	}

	@EventHandler
	public void c2(BlockPistonRetractEvent event) {
		BlockFace direction = event.getDirection();
		Block movedBlock = event.getBlock().getRelative(direction);
		movedBlock.setMetadata("player_placed", new FixedMetadataValue(MMOCore.plugin, true));

		for (Block block : event.getBlocks()) {
			movedBlock = block.getRelative(direction);
			movedBlock.setMetadata("player_placed", new FixedMetadataValue(MMOCore.plugin, true));
		}
	}

	private Location getSafeDropLocation(Block block, boolean self) {
		if (block.getType() == Material.AIR && self)
			return block.getLocation();

		Block relative;
		for (BlockFace face : order)
			if (!(relative = block.getRelative(face)).getType().isSolid())
				return relative.getLocation().add(block.getLocation().subtract(relative.getLocation()).multiply(.3));
		return block.getLocation();
	}
}
