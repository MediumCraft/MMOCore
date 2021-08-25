package net.Indyuce.mmocore.listener;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.utils.Schedulers;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.block.BlockInfo;
import net.Indyuce.mmocore.api.block.BlockInfo.BlockInfoOption;
import net.Indyuce.mmocore.api.block.VanillaBlockType;
import net.Indyuce.mmocore.loot.droptable.condition.ConditionInstance;
import net.Indyuce.mmocore.api.event.CustomBlockMineEvent;
import net.Indyuce.mmocore.loot.LootBuilder;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.List;

public class BlockListener implements Listener {
    private static final BlockFace[] order = {BlockFace.UP, BlockFace.DOWN, BlockFace.EAST, BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH};

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCustomBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE)
            return;

        String savedData = event.getBlock().getBlockData().getAsString();
        Block block = event.getBlock();

        /*
         * Check for custom mining in the current region first.
         */
        boolean customMine = MMOCore.plugin.mineManager.isEnabled(player, block.getLocation());
        if (!customMine)
            return;

        /*
         * If the block is a temporary block placed by block regen, immediately
         * cancel the break event; also check for extra config provided block
         * conditions
         */
        BlockInfo info = MMOCore.plugin.mineManager.getInfo(block);
        boolean temporaryBlock = MMOCore.plugin.mineManager.isTemporaryBlock(block);
        if ((temporaryBlock && info == null) || (info != null && !info.checkConditions(block))) {
            event.setCancelled(true);
            return;
        }

        if (info == null) {

            /*
             * If players are prevented from breaking blocks in custom mining
             * regions
             */
            if (MMOCore.plugin.mineManager.shouldProtect())
                event.setCancelled(true);

            return;
        }

        /*
         * Extra breaking conditions.
         */
        if (!info.getBlock().breakRestrictions(block)) {
            event.setCancelled(true);
            return;
        }

        boolean canBreak = true;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!MMOCore.plugin.restrictionManager.checkPermissions(item, info.getBlock())) {
            MMOCore.plugin.configManager.getSimpleMessage("cannot-break").send(player);
            canBreak = false;
        }

        /*
         * Find the block drops
         */
        boolean conditionsMet = !info.hasDropTable() || info.getDropTable().areConditionsMet(new ConditionInstance(player));
        List<ItemStack> drops = conditionsMet ? info.getDropTable().collect(new LootBuilder(PlayerData.get(player), 0)) : new ArrayList<>();

        /*
         * Calls the event and listen for cancel & for drops changes... also
         * allows to apply tool durability & enchants to drops, etc.
         */
        CustomBlockMineEvent called = new CustomBlockMineEvent(PlayerData.get(player), block, info, drops, !canBreak);
        Bukkit.getPluginManager().callEvent(called);
        if (called.isCancelled()) {
            event.setCancelled(true);
            return;
        }

        /*
         * Remove vanilla drops if needed and
         * decreases the durability of the item
         * used to mine the block.
         */
        if (!info.getOption(BlockInfoOption.VANILLA_DROPS)) {
            event.setCancelled(true);
            event.getBlock().setType(Material.AIR);
            MMOCoreUtils.decreaseDurability(player, EquipmentSlot.HAND, 1);
        }

        /*
         * Apply triggers, add experience info to the event so the other events
         * can give exp to other TOOLS and display HOLOGRAMS
         */
        if (conditionsMet && info.hasTriggers() && !block.hasMetadata("player_placed")) {
            PlayerData playerData = PlayerData.get(player);
            info.getTriggers().forEach(trigger -> trigger.apply(playerData));
        }

        /*
         * Apply drop tables
         *
         * You can apply the drop tables even if the block was placed by a player.
         */
        if (conditionsMet && info.hasDropTable()) {
            Location dropLocation = getSafeDropLocation(block, !block.getType().isSolid() || !(info.regenerates() && info.getRegenerationInfo().hasTemporaryBlock()));
            for (ItemStack drop : drops)
                if (drop.getType() != Material.AIR && drop.getAmount() > 0)
                    UtilityMethods.dropItemNaturally(dropLocation, drop);
        }

        /*
         * Finally enable block regen.
         */
        if (info.hasRegen())
            Schedulers.sync().runLater(() -> MMOCore.plugin.mineManager.initialize(info.startRegeneration(Bukkit.createBlockData(savedData), block.getLocation()), !temporaryBlock), 1);
    }

    /*
     * This is handled in a separate event because it
     * needs to happen AFTER it's already checked the tag
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void unregisterPlayerPlacedBlocksTag(BlockBreakEvent event) {
        if (event.getBlock().hasMetadata("player_placed"))
            event.getBlock().removeMetadata("player_placed", MMOCore.plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void registerPlayerPlacedBlocksTag(BlockPlaceEvent event) {
        event.getBlock().setMetadata("player_placed", new FixedMetadataValue(MMOCore.plugin, true));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void blockPistonExtend(BlockPistonExtendEvent event) {
        Block movedBlock = event.getBlock();
        if (!movedBlock.hasMetadata("player_placed"))
            return;
        BlockFace direction = event.getDirection();
        // movedBlock = movedBlock.getRelative(direction, 2);

        for (Block b : event.getBlocks())
            if (b.hasMetadata("player_placed")) {
                movedBlock = b.getRelative(direction);
                movedBlock.setMetadata("player_placed", new FixedMetadataValue(MMOCore.plugin, true));
            }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void blockPistonRetract(BlockPistonRetractEvent event) {
        BlockFace direction = event.getDirection();
        Block movedBlock = event.getBlock().getRelative(direction);
        movedBlock.setMetadata("player_placed", new FixedMetadataValue(MMOCore.plugin, true));

        for (Block block : event.getBlocks()) {
            movedBlock = block.getRelative(direction);
            movedBlock.setMetadata("player_placed", new FixedMetadataValue(MMOCore.plugin, true));
        }
    }

    /*
     * Allows to mark cobblestone generated by cobblestone generators so that
     * exp is not gained by these blocks
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void cobblestoneGeneratorHandling(BlockFormEvent event) {
        if (event.getBlock().hasMetadata("player_placed"))
            event.getBlock().removeMetadata("player_placed", MMOCore.plugin);
        if (MMOCore.plugin.configManager.cobbleGeneratorXP) return;

        if (event.getBlock().getType() == Material.WATER || event.getBlock().getType() == Material.LAVA)
            if (event.getNewState().getType() == Material.COBBLESTONE || event.getNewState().getType() == Material.OBSIDIAN)
                event.getNewState().setMetadata("player_placed", new FixedMetadataValue(MMOCore.plugin, true));
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
