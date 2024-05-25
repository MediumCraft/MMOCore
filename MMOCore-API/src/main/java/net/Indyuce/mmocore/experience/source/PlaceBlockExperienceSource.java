package net.Indyuce.mmocore.experience.source;

import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.experience.dispenser.ExperienceDispenser;
import net.Indyuce.mmocore.experience.source.type.SpecificExperienceSource;
import net.Indyuce.mmocore.manager.profession.ExperienceSourceManager;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;

public class PlaceBlockExperienceSource extends SpecificExperienceSource<Material> {
    public final Material material;

    public PlaceBlockExperienceSource(ExperienceDispenser dispenser, MMOLineConfig config) {
        super(dispenser, config);

        config.validate("type");
        material = Material.valueOf(config.getString("type").toUpperCase().replace("-", "_").replace(" ", "_"));
    }

    @Override
    public ExperienceSourceManager<PlaceBlockExperienceSource> newManager() {
        return new Manager();
    }

    @Override
    public boolean matchesParameter(PlayerData player, Material obj) {
        return material == obj;
    }


    private static class Manager extends ExperienceSourceManager<PlaceBlockExperienceSource> {

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void a(BlockPlaceEvent event) {
            if (event.getPlayer().getGameMode() != GameMode.SURVIVAL)
                return;

            PlayerData data = PlayerData.get(event.getPlayer());
            for (PlaceBlockExperienceSource source : getSources())
                if (source.matches(data, event.getBlock().getType()))
                    source.giveExperience(data, 1, event.getBlock().getLocation());
        }
    }
}
