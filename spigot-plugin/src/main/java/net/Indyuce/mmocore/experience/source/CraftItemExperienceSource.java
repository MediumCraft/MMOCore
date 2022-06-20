package net.Indyuce.mmocore.experience.source;

import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.experience.dispenser.ExperienceDispenser;
import net.Indyuce.mmocore.experience.source.type.SpecificExperienceSource;
import net.Indyuce.mmocore.manager.profession.ExperienceSourceManager;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

public class CraftItemExperienceSource extends SpecificExperienceSource<Material> {
    public final Material material;

    public CraftItemExperienceSource(ExperienceDispenser dispenser, MMOLineConfig config) {
        super(dispenser, config);

        config.validate("type");
        material = Material.valueOf(config.getString("type").toUpperCase().replace("-", "_").replace(" ", "_"));
    }

    @Override
    public ExperienceSourceManager<CraftItemExperienceSource> newManager() {
        return new ExperienceSourceManager<CraftItemExperienceSource>() {
            @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
            public void a(CraftItemEvent event) {
                if (event.getAction() == InventoryAction.NOTHING ||
                        event.getInventory().getResult() == null) return;

                PlayerData data = PlayerData.get((Player) event.getWhoClicked());

                /**
                 * This makes sure that the crafting recipe was performed correctly.
                 *
                 * In some scenarii, the CraftItemEvent (which is only a click event
                 * by the way) is not cancelled, but the item is not crafted which makes
                 * EXP duplication a game breaking glitch. MMOCore make sure that at
                 * least one ingredient has lowered in amount.
                 *
                 * The second objective of that check is to deduce the amount of items that
                 * were crafted during this event. For that, it finds the item with the lowest
                 * amount in the crafting matrix and see how many items disappeared,
                 * multiplied by the recipe output amount (works for a shift click).
                 *
                 * References:
                 * - https://git.lumine.io/mythiccraft/mmocore/-/issues/102
                 * - https://www.spigotmc.org/threads/how-to-get-amount-of-item-crafted.377598/
                 */
                final int index = getLowerAmountIngredientIndex(event.getInventory().getMatrix());
                final int oldAmount = event.getInventory().getMatrix()[index].getAmount();
                final int itemsCraftedPerRecipe = event.getInventory().getResult().getAmount();
                final Material resultType = event.getInventory().getResult().getType();

                Bukkit.getScheduler().runTask(MMOCore.plugin, () -> {

                    // First check
                    int newAmount = getAmount(event.getInventory().getMatrix()[index]);
                    if (newAmount >= oldAmount)
                        return;

                    // Deduce amount crafted
                    int amountCrafted = (event.getClick().isShiftClick() ? oldAmount - newAmount : 1) * itemsCraftedPerRecipe;
                    for (CraftItemExperienceSource source : getSources())
                        if (source.matches(data, resultType))
                            source.giveExperience(data, amountCrafted, event.getInventory().getLocation());
                });
            }
        };
    }

    @Override
    public boolean matchesParameter(PlayerData player, Material obj) {
        return material == obj;
    }

    private int getAmount(@Nullable ItemStack item) {
        return item == null || item.getType() == Material.AIR ? 0 : item.getAmount();
    }

    private int getLowerAmountIngredientIndex(ItemStack[] matrix) {
        int lower = Integer.MAX_VALUE;
        int index = -1;

        for (int i = 0; i < matrix.length; i++) {
            ItemStack checked = matrix[i];
            if (checked != null && checked.getType() != Material.AIR && checked.getAmount() > 0 && checked.getAmount() < lower) {
                lower = checked.getAmount();
                index = i;
            }
        }

        Validate.isTrue(index != -1, "No item in matrix");
        return index;
    }
}
