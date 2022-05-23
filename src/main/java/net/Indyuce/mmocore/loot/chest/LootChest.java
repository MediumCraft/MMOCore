package net.Indyuce.mmocore.loot.chest;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.SoundEvent;
import net.Indyuce.mmocore.util.item.HashableLocation;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.data.BlockData;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;

public class LootChest {
    private final ChestTier tier;
    private final LootChestRegion region;
    private final ReplacedBlock block;
    @Nullable
    private final BukkitRunnable effectRunnable;
    private final BukkitRunnable closeRunnable;

    private boolean active = true;

    /**
     * Called when a loot chest is placed as a Bukkit block, and used
     * to save the data of the block which has been replaced.
     * <p>
     * A placed drop chest may only replace non solid blocks like grass
     * or levels..
     */
    public LootChest(ChestTier tier, LootChestRegion region, Block block) {
        this.tier = tier;
        this.region = region;
        this.block = new ReplacedBlock(block);
        this.effectRunnable = tier.hasEffect() ? tier.getEffect().startNewRunnable(block.getLocation().add(.5, .5, .5)) : null;
        closeRunnable = new BukkitRunnable() {
            @Override
            public void run() {
                expire(false);
            }
        };
        closeRunnable.runTaskLater(MMOCore.plugin, MMOCore.plugin.configManager.lootChestExpireTime);
    }

    public ChestTier getTier() {
        return tier;
    }

    public ReplacedBlock getBlock() {
        return block;
    }

    public LootChestRegion getRegion() {
        return region;
    }

    public boolean isActive() {
        return active;
    }

    /**
     * This does NOT remove the loot chest from the plugin registry.
     *
     * @param player If a player triggered the unregistration of that chest by
     *               opening and then closing it for the first time. It's set
     *               to false when a loot chest expires or when MMOCore disables.
     *               <p>
     *               When no player is closing the chest, its content should be lost
     */
    public void expire(boolean player) {

        // Check for expire
        Validate.isTrue(active, "Chest has already expired");
        active = false;

        // Close runnable
        if (!closeRunnable.isCancelled())
            closeRunnable.cancel();

        // If a player is responsible of closing the chest, close it with sound
        if (player) {
            MMOCore.plugin.soundManager.getSound(SoundEvent.CLOSE_LOOT_CHEST).playAt(block.loc.bukkit());
            block.loc.getWorld().spawnParticle(Particle.CRIT, block.loc.bukkit().add(.5, .5, .5), 16, 0, 0, 0, .5);
        }

        /*
         * Must clean block inventory before replacing block otherwise loots fly
         * off and accumulate on the ground (+during dev phase)
         */
        else
            ((Chest) block.loc.bukkit().getBlock().getState()).getBlockInventory().clear();

        block.restore();
        if (effectRunnable != null)
            effectRunnable.cancel();
    }

    public static class ReplacedBlock {
        private final Material material;
        private final BlockData data;
        private final HashableLocation loc;

        public ReplacedBlock(Block block) {
            this.material = block.getType();
            this.data = block.getBlockData();
            this.loc = new HashableLocation(block.getLocation());
        }

        public HashableLocation getLocation() {
            return loc;
        }

        @Deprecated
        public boolean matches(Location loc) {
            return this.loc.getWorld().equals(loc.getWorld()) && this.loc.getX() == loc.getBlockX() && this.loc.getY() == loc.getBlockY()
                    && this.loc.getZ() == loc.getBlockZ();
        }

        public void restore() {
            Block block = loc.bukkit().getBlock();
            block.setType(material);
            block.setBlockData(data);
        }
    }
}
