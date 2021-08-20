package net.Indyuce.mmocore.experience.source;

import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.experience.provider.ExperienceDispenser;
import net.Indyuce.mmocore.experience.source.type.SpecificExperienceSource;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.manager.profession.ExperienceSourceManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryAction;

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
                        source.giveExperience(data, event.getInventory().getResult().getAmount(), event.getInventory().getLocation());
            }
        };
    }

    @Override
    public boolean matchesParameter(PlayerData player, Material obj) {
        return material == obj;
    }
}
