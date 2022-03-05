package net.Indyuce.mmocore.listener.profession;

import io.lumine.mythic.lib.version.VersionSound;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.event.CustomPlayerFishEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.stats.StatType;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import net.Indyuce.mmocore.experience.EXPSource;
import net.Indyuce.mmocore.loot.LootBuilder;
import net.Indyuce.mmocore.loot.droptable.dropitem.fishing.FishingDropItem;
import net.Indyuce.mmocore.manager.profession.FishingManager.FishingDropTable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class FishingListener implements Listener {
    private final Set<UUID> fishing = new HashSet<>();

    private static final Random RANDOM = new Random();

    @EventHandler(priority = EventPriority.LOW)
    public void a(PlayerFishEvent event) {
        Player player = event.getPlayer();
        FishHook hook = event.getHook();

        if (event.getState() == State.BITE && !fishing.contains(player.getUniqueId()) && !player.hasMetadata("NPC")) {

            /*
             * Checks for drop tables. If no drop table, just plain vanilla
             * fishing OTHERWISE initialize fishing, register other listener.
             */
            FishingDropTable table = MMOCore.plugin.fishingManager.calculateDropTable(player);
            if (table == null)
                return;

            new FishingData(player, hook, table);
            MMOCoreUtils.displayIndicator(hook.getLocation().add(0, 1.25, 0), MMOCore.plugin.configManager.getSimpleMessage("caught-fish").message());
        }
    }

    public class FishingData extends BukkitRunnable implements Listener {
        private final Location location;
        private final FishingDropItem caught;
        private final PlayerData playerData;
        private final Player player;
        private final FishHook hook;

        private final int fishStrength, experienceDropped;

        private int currentPulls;

        /**
         * Used to track the last time the player swung the fishing rod.
         * If the player does not swing the rod at least once every second,
         * the fish will go away and drops will be lost.
         */
        private long last = System.currentTimeMillis();

        private static final long TIME_OUT = 1000;

        public FishingData(Player player, FishHook hook, FishingDropTable table) {
            this.location = hook.getLocation();
            this.caught = table.getRandomItem();
            this.playerData = PlayerData.get(this.player = player);
            this.hook = hook;

            this.fishStrength = (int) Math.floor(caught.rollTugs() * (1 - PlayerData.get(player).getStats().getStat(StatType.FISHING_STRENGTH) / 100));
            this.experienceDropped = caught.rollExperience();

            fishing.add(player.getUniqueId());
            runTaskTimer(MMOCore.plugin, 0, 2);
            Bukkit.getPluginManager().registerEvents(this, MMOCore.plugin);

            // Check for instant loot
            if (fishStrength == 0)
                lootFish();
        }

        public void criticalFish() {
            currentPulls = fishStrength + 2;
        }

        public boolean isTimedOut() {
            return last + TIME_OUT < System.currentTimeMillis();
        }

        /**
         * @return If the fish is weak enough to be looted by the player.
         */
        public boolean pull() {
            last = System.currentTimeMillis();
            currentPulls++;
            return currentPulls >= fishStrength;
        }

        /**
         * Critical fish's means you catch the fish on the very first try
         */
        public boolean isCrit() {
            return currentPulls > fishStrength + 1;
        }

        private void close() {
            fishing.remove(player.getUniqueId());
            hook.remove();

            HandlerList.unregisterAll(this);
            cancel();
        }

        @Override
        public void run() {
            if (isTimedOut())
                close();

            location.getWorld().spawnParticle(Particle.CRIT, location, 0, 2 * (RANDOM.nextDouble() - .5), 3, 2 * (RANDOM.nextDouble() - .5), .6);
        }

        @EventHandler
        public void a(PlayerFishEvent event) {
            if (event.getPlayer().equals(player) && (event.getState() == State.CAUGHT_FISH || event.getState() == State.FAILED_ATTEMPT || event.getState() == State.REEL_IN)) {

                // Lose the catch if the current fish is gone!
                event.setCancelled(true);
                if (isTimedOut()) {
                    close();
                    hook.getWorld().spawnParticle(Particle.SMOKE_NORMAL, hook.getLocation(), 16, 0, 0, 0, .1);
                    return;
                }

                System.out.println("Pulls: " + currentPulls + " / " + fishStrength);

                if (currentPulls == 0 && RANDOM.nextDouble() < PlayerData.get(player).getStats().getStat(StatType.CRITICAL_FISHING_CHANCE) / 100)
                    criticalFish();

                // Check if enough pulls; if not, wait till the next fish event
                if (pull())
                    lootFish();
            }
        }

        public void lootFish() {
            close();

            ItemStack mainhand = player.getInventory().getItem(EquipmentSlot.HAND);
            MMOCoreUtils.decreaseDurability(player,
                    (mainhand != null && mainhand.getType() == Material.FISHING_ROD) ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND, 1);

            // Critical fishing failure
            if (!isCrit() && RANDOM.nextDouble() < PlayerData.get(player).getStats().getStat(StatType.CRITICAL_FISHING_FAILURE_CHANCE) / 100) {
                player.setVelocity(hook.getLocation().subtract(player.getLocation()).toVector().setY(0).multiply(3).setY(.5));
                hook.getWorld().spawnParticle(Particle.SMOKE_NORMAL, location, 24, 0, 0, 0, .08);
                return;
            }

            // Find looted item
            ItemStack collect = caught.collect(new LootBuilder(playerData, 0));
            if (collect == null) {
                hook.getWorld().spawnParticle(Particle.SMOKE_NORMAL, location, 24, 0, 0, 0, .08);
                return;
            }

            // Call Bukkit event
            CustomPlayerFishEvent called = new CustomPlayerFishEvent(playerData, collect);
            Bukkit.getPluginManager().callEvent(called);
            if (called.isCancelled())
                return;

            // Calculate yeet velocity
            Item item = hook.getWorld().dropItemNaturally(hook.getLocation(), collect);
            MMOCoreUtils.displayIndicator(location.add(0, 1.25, 0),
                    MMOCore.plugin.configManager.getSimpleMessage("fish-out-water" + (isCrit() ? "-crit" : "")).message());
            Vector vec = player.getLocation().subtract(hook.getLocation()).toVector();
            vec.setY(vec.getY() * .031 + vec.length() * .05);
            vec.setX(vec.getX() * .08);
            vec.setZ(vec.getZ() * .08);
            item.setVelocity(vec);
            player.getWorld().playSound(player.getLocation(), VersionSound.BLOCK_NOTE_BLOCK_HAT.toSound(), 1, 0);
            for (int j = 0; j < 8; j++)
                location.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, location, 0, 4 * (RANDOM.nextDouble() - .5), RANDOM.nextDouble() + 1, 4 * (RANDOM.nextDouble() - .5), .08);

            if (MMOCore.plugin.fishingManager.hasLinkedProfession())
                playerData.getCollectionSkills().giveExperience(MMOCore.plugin.fishingManager.getLinkedProfession(), experienceDropped, EXPSource.FISHING, location);
        }
    }
}
