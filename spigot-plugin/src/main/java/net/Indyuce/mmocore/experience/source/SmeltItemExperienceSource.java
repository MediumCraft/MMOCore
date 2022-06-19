package net.Indyuce.mmocore.experience.source;

import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.experience.dispenser.ExperienceDispenser;
import net.Indyuce.mmocore.experience.source.type.SpecificExperienceSource;
import net.Indyuce.mmocore.manager.profession.ExperienceSourceManager;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockCookEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class SmeltItemExperienceSource extends SpecificExperienceSource<ItemStack> {
    private final Material material;

    public SmeltItemExperienceSource(ExperienceDispenser dispenser, MMOLineConfig config) {
        super(dispenser, config);

        config.validate("type");
        material = Material.valueOf(config.getString("type").toUpperCase().replace("-", "_").replace(" ", "_"));
    }

    @Override
    public ExperienceSourceManager<SmeltItemExperienceSource> newManager() {
        return new ExperienceSourceManager<SmeltItemExperienceSource>() {

            @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
            public void a(BlockCookEvent event) {
                Optional<Player> player = getNearestPlayer(event.getBlock().getLocation());
                if (!player.isPresent())
                    return;

                ItemStack caught = event.getResult();
                if (caught.hasItemMeta())
                    return;

                PlayerData data = PlayerData.get(player.get());
                for (SmeltItemExperienceSource source : getSources())
                    if (source.matches(data, caught))
                        source.giveExperience(data, 1, event.getBlock().getLocation());
            }
        };
    }

    private Optional<Player> getNearestPlayer(Location loc) {
        final Player[] nearby = loc.getWorld().getPlayers().stream().filter(player -> player.getLocation().distanceSquared(loc) < 100)
                .toArray(Player[]::new);
        Player selected = null;
        double lastDist = 100;
        for (Player p : nearby) {
            double currDist = p.getLocation().distance(loc);
            if (currDist < lastDist) {
                lastDist = currDist;
                selected = p;
            }
        }
        return Optional.ofNullable(selected);
    }

    @Override
    public boolean matchesParameter(PlayerData player, ItemStack obj) {
        return obj.getType() == material;
    }
}
