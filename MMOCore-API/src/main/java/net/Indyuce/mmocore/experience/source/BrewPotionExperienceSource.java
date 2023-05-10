package net.Indyuce.mmocore.experience.source;

import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.experience.source.type.ExperienceSource;
import net.Indyuce.mmocore.experience.EXPSource;
import net.Indyuce.mmocore.experience.dispenser.ExperienceDispenser;
import net.Indyuce.mmocore.manager.profession.ExperienceSourceManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BrewPotionExperienceSource extends ExperienceSource<PotionMeta> {
    private final double multiplier;
    private final List<PotionType> types = new ArrayList<>();

    public BrewPotionExperienceSource(ExperienceDispenser dispenser, MMOLineConfig config) {
        super(dispenser);

        multiplier = config.getDouble("multiplier", 1);

        if (config.contains("effect"))
            for (String key : config.getString("effect").split(","))
                types.add(PotionType.valueOf(key.toUpperCase().replace("-", "_")));
    }

    @Override
    public boolean matchesParameter(PlayerData player, PotionMeta meta) {
        return types.isEmpty() || new ArrayList<>(types).stream().anyMatch(type -> meta.getBasePotionData().getType() == type);
    }

    @Override
    public ExperienceSourceManager<BrewPotionExperienceSource> newManager() {
        return new ExperienceSourceManager<BrewPotionExperienceSource>() {

            @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
            public void a(BrewEvent event) {
                Optional<Player> playerOpt = getNearbyPlayer(event.getBlock().getLocation());
                if (!playerOpt.isPresent())
                    return;

                final ItemStack found = findPotion(event.getContents());
                if (found != null)
                    Bukkit.getScheduler().scheduleSyncDelayedTask(MMOCore.plugin, () -> {
                        ItemStack brewn = findPotion(event.getContents());
                        if (brewn == null)
                            return;

                        PlayerData data = PlayerData.get(playerOpt.get());
                        for (BrewPotionExperienceSource source : getSources())
                            if (source.matches(data, (PotionMeta) brewn.getItemMeta()))
                                new PotionUpgrade(found, brewn).process(data.getPlayer());
                    });
            }
        };
    }

    private ItemStack findPotion(BrewerInventory inv) {
        for (int j = 0; j < 3; j++) {
            ItemStack item = inv.getItem(j);
            if (item != null && item.hasItemMeta() && item.getItemMeta() instanceof PotionMeta)
                return item;
        }
        return null;
    }

    private Optional<Player> getNearbyPlayer(Location loc) {
        return loc.getWorld().getPlayers().stream().filter(player -> player.getLocation().distanceSquared(loc) < 100).findAny();
    }

    public class PotionUpgrade {

        /*
         * if the potion was extended using redstone or upgraded using
         * glowstone. PREPARE corresponds to when a water bottle is prepared for
         * later recipes using NETHER STALK
         */
        private double exp;

        // private final PotionMeta old, brewn;

        public PotionUpgrade(ItemStack old, ItemStack brewn) {
            this(old.getType(), (PotionMeta) old.getItemMeta(), brewn.getType(), (PotionMeta) brewn.getItemMeta());
        }

        public PotionUpgrade(Material oldPot, PotionMeta old, Material brewnPot, PotionMeta brewn) {

            // this.old = old;
            // this.brewn = brewn;

            /*
             * calculate base exp
             */

            exp += MMOCore.plugin.alchemyManager.getBaseExperience(brewn.getBasePotionData().getType());

            // !old.getBasePotionData().getType().isUpgradeable() &&
            // brewn.getBasePotionData().getType().isUpgradeable(),
            //
            // !old.getBasePotionData().isExtended() &&
            // brewn.getBasePotionData().isExtended(),
            //
            // !old.getBasePotionData().isUpgraded() &&
            // brewn.getBasePotionData().isUpgraded());

            /*
             * EXP modifiers based on brewing conditions
             */
            if (oldPot == Material.POTION && brewnPot == Material.SPLASH_POTION)
                exp *= MMOCore.plugin.alchemyManager.splash;
            if (oldPot == Material.POTION && brewnPot == Material.LINGERING_POTION)
                exp *= MMOCore.plugin.alchemyManager.lingering;
            if (!old.getBasePotionData().isExtended() && brewn.getBasePotionData().isExtended())
                exp *= MMOCore.plugin.alchemyManager.extend;
            if (!old.getBasePotionData().isUpgraded() && brewn.getBasePotionData().isUpgraded())
                exp *= MMOCore.plugin.alchemyManager.upgrade;
        }

        // private Map<PotionEffectType, Double> mapEffectDurations() {
        // Map<PotionEffectType, Double> map = new HashMap<>();
        //
        // /*
        // * potion level, plus the potion gained duration (max 0 so it does
        // * not give negative EXP), multiplied by the potion effect weight.
        // */
        // brewn.getCustomEffects().forEach(effect -> map.put(effect.getType(),
        //
        // (effect.getAmplifier() + 1 + (double) Math.max(0,
        // effect.getDuration() - getPotionEffect(old,
        // effect.getType()).orElseGet(() -> new
        // PotionEffect(PotionEffectType.SPEED, 0, 0)).getDuration()) / 60.)
        //
        // * MMOCore.plugin.alchemyManager.getWeight(effect.getType())));
        //
        // return map;
        // }

        // private int getTotal(Map<?, Double> map) {
        // double t = 0;
        // for (double d : map.values())
        // t += d;
        // return (int) t;
        // }

        // private Optional<PotionEffect> getPotionEffect(PotionMeta meta,
        // PotionEffectType type) {
        // return meta.getCustomEffects().stream().filter(effect ->
        // effect.getType() == type).findFirst();
        // }

        public void process(Player player) {

            /*
             * calculate extra exp due to extra effects
             */
            // exp += getTotal(mapEffectDurations());

            getDispenser().giveExperience(PlayerData.get(player), exp * multiplier, player.getLocation(), EXPSource.SOURCE);
        }
    }
}
