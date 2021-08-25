package net.Indyuce.mmocore.experience.source;

import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.experience.provider.ExperienceDispenser;
import net.Indyuce.mmocore.experience.source.type.SpecificExperienceSource;
import net.Indyuce.mmocore.manager.profession.ExperienceSourceManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;

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
                for (CraftItemExperienceSource source : getSources())
                    if (source.matches(data, event.getInventory().getResult().getType()))
                        source.giveExperience(data, getAmountCrafted(event), event.getInventory().getLocation());
            }
        };
    }

    @Override
    public boolean matchesParameter(PlayerData player, Material obj) {
        return material == obj;
    }

    /**
     * Source:
     * - https://www.spigotmc.org/threads/how-to-get-amount-of-item-crafted.377598/
     * <p>
     * The idea is to find the item with the lowest count which is basically
     * the amount of items that are crafted. Minecraft uses the same calculation
     *
     * @return Amount of items crafted
     */
    private final int getAmountCrafted(CraftItemEvent event) {
        ItemStack craftedItem = event.getInventory().getResult(); //Get result of recipe
        ClickType clickType = event.getClick();

        // No shift click
        if (!clickType.isShiftClick())
            return craftedItem.getAmount();

        // Find lowest amount of all ingredients
        int lowerAmount = craftedItem.getMaxStackSize() + 1000; //Set lower at recipe result max stack size + 1000 (or just highter max stacksize of reciped item)
        for (ItemStack actualItem : event.getInventory().getContents()) //For each item in crafting inventory
            if (!actualItem.getType().isAir() && lowerAmount > actualItem.getAmount() && !actualItem.getType().equals(craftedItem.getType())) //if slot is not air && lowerAmount is highter than this slot amount && it's not the recipe amount
                lowerAmount = actualItem.getAmount(); //Set new lower amount

        //Calculate the final amount : lowerAmount * craftedItem.getAmount
        return lowerAmount * craftedItem.getAmount();
    }
}
