package net.Indyuce.mmocore.experience.source;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.experience.EXPSource;
import net.Indyuce.mmocore.experience.dispenser.ExperienceDispenser;
import net.Indyuce.mmocore.experience.source.type.ExperienceSource;
import net.Indyuce.mmocore.manager.profession.ExperienceSourceManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import javax.annotation.Nullable;

public class RepairItemExperienceSource extends ExperienceSource<ItemStack> {
    @Nullable
    private final Material material;

    public RepairItemExperienceSource(ExperienceDispenser dispenser, MMOLineConfig config) {
        super(dispenser);

        /*
         * If material is null, the player can repair
         * ANY material in order to get experience.
         */
        material = config.contains("type") ? Material.valueOf(config.getString("type").toUpperCase().replace("-", "_").replace(" ", "_")) : null;
    }

    @Override
    public boolean matchesParameter(PlayerData player, ItemStack item) {
        return material == null || item.getType() == material;
    }

    @Override
    public ExperienceSourceManager<RepairItemExperienceSource> newManager() {
        return new CustomExperienceManager();
    }

    private class CustomExperienceManager extends ExperienceSourceManager<RepairItemExperienceSource> {

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void a(InventoryClickEvent event) {
            if (event.getInventory() == null || event.getInventory().getType() != InventoryType.ANVIL || event.getRawSlot() != 2)
                return;

            // Check if there's exp associated to it
            final @Nullable ItemStack item = event.getCurrentItem();
            if (UtilityMethods.isAir(item)) return;
            if (!MMOCore.plugin.smithingManager.hasExperience(item.getType())) return;

            final PlayerData data = PlayerData.get((Player) event.getWhoClicked());
            for (RepairItemExperienceSource source : getSources())
                if (source.matches(data, item)) {

                    if (!(event.getInventory() instanceof AnvilInventory))
                        return;

                    if (((AnvilInventory) event.getInventory()).getRepairCost() > ((Player) event.getWhoClicked()).getLevel())
                        return;

                    /*
                     * Make sure the items can actually be repaired
                     * before getting the amount of durability repaired
                     */
                    final ItemStack old = event.getInventory().getItem(0);
                    if (old == null || old.getType() == Material.AIR || old.getType().getMaxDurability() < 30 || item.getType().getMaxDurability() < 10)
                        return;

                    /*
                     * Calculate exp based on amount of durability which was repaired,
                     * substract damage from old item durability.
                     */
                    final double exp = MMOCore.plugin.smithingManager.getBaseExperience(item.getType())
                            * Math.max(0, ((Damageable) old.getItemMeta()).getDamage() - ((Damageable) item.getItemMeta()).getDamage()) / 100;
                    getDispenser().giveExperience(data, exp, data.getPlayer().getLocation(), EXPSource.SOURCE);
                }
        }
    }
}
