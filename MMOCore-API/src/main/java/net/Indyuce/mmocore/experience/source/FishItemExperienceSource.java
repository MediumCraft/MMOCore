package net.Indyuce.mmocore.experience.source;

import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.experience.dispenser.ExperienceDispenser;
import net.Indyuce.mmocore.experience.source.type.SpecificExperienceSource;
import net.Indyuce.mmocore.manager.profession.ExperienceSourceManager;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.inventory.ItemStack;

public class FishItemExperienceSource extends SpecificExperienceSource<ItemStack> {
    private final Material material;

    public FishItemExperienceSource(ExperienceDispenser dispenser, MMOLineConfig config) {
        super(dispenser, config);

        config.validate("type");
        material = Material.valueOf(config.getString("type").toUpperCase().replace("-", "_").replace(" ", "_"));
    }

    @Override
    public ExperienceSourceManager<FishItemExperienceSource> newManager() {
        return new Manager();
    }

    @Override
    public boolean matchesParameter(PlayerData player, ItemStack obj) {
        return obj.getType() == material;
    }

    private static class Manager extends ExperienceSourceManager<FishItemExperienceSource> {

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void a(PlayerFishEvent event) {
            if (event.getState() == State.CAUGHT_FISH) {
                ItemStack caught = ((Item) event.getCaught()).getItemStack();
                if (caught.hasItemMeta())
                    return;

                PlayerData data = PlayerData.get(event.getPlayer());
                for (FishItemExperienceSource source : getSources())
                    if (source.matches(data, caught))
                        source.giveExperience(data, caught.getAmount(), event.getHook().getLocation().add(0, 1.0f, 0));
            }
        }
    }
}
