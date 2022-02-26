package net.Indyuce.mmocore.loot.chest;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.SoundEvent;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;

public class LootChest {
    private final ChestTier tier;
    private final LootChestRegion region;
    private final ReplacedBlock block;
    @Nullable
    private final BukkitRunnable effectRunnable;
    private final long date = System.currentTimeMillis();

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

    public boolean hasPlayerNearby() {
        for (Player player : block.loc.getWorld().getPlayers())
            if (player.getLocation().distanceSquared(block.loc) < 625)
                return true;
        return false;
    }

    public boolean shouldExpire() {
        return System.currentTimeMillis() - date > MMOCore.plugin.configManager.lootChestExpireTime;
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

        // If a player is responsible of closing the chest, close it with sound
        if (player) {
            MMOCore.plugin.soundManager.getSound(SoundEvent.CLOSE_LOOT_CHEST).playAt(block.loc);
            block.loc.getWorld().spawnParticle(Particle.CRIT, block.loc.clone().add(.5, .5, .5), 16, 0, 0, 0, .5);
        }

        /*
         * Must clean block inventory before replacing block otherwise loots fly
         * off and accumulate on the ground (+during dev phase)
         */
        else
            ((Chest) block.loc.getBlock().getState()).getBlockInventory().clear();

        block.restore();
        if (effectRunnable != null)
            effectRunnable.cancel();
    }

    public static class ReplacedBlock {
        private final Material material;
        private final BlockData data;
        private final Location loc;

        public ReplacedBlock(Block block) {
            this.material = block.getType();
            this.data = block.getBlockData();
            this.loc = block.getLocation();
        }

        public boolean matches(Location loc) {
            return this.loc.getWorld().equals(loc.getWorld()) && this.loc.getBlockX() == loc.getBlockX() && this.loc.getBlockY() == loc.getBlockY()
                    && this.loc.getBlockZ() == loc.getBlockZ();
        }

        public void restore() {
            loc.getBlock().setType(material);
            loc.getBlock().setBlockData(data);
        }
    }
}
