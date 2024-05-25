package net.Indyuce.mmocore.experience.source;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.experience.dispenser.ExperienceDispenser;
import net.Indyuce.mmocore.experience.source.type.SpecificExperienceSource;
import net.Indyuce.mmocore.manager.profession.ExperienceSourceManager;
import org.apache.commons.lang.Validate;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.bukkit.event.EventPriority.HIGHEST;

public class RideExperienceSource extends SpecificExperienceSource<EntityType> {
    private final EntityType type;

    /**
     * Gives experience when a player moves riding a certain entity. If no entity type is given it will give xp if you move
     * while riding an entity whatever it is.
     * The random value you give correspond to the xp you get per block travelled while riding.
     */
    public RideExperienceSource(ExperienceDispenser dispenser, MMOLineConfig config) {
        super(dispenser, config);
        if (!config.contains("type"))
            type = null;
        else {
            String str = config.getString("type").toUpperCase().replace("-", "_");
            Validate.isTrue(Arrays.stream(EntityType.values()).map(Objects::toString).collect(Collectors.toList()).contains(str),
                    "The type must correspond to an entity that exist in the game.");
            type = EntityType.valueOf(str);
        }

    }

    @Override
    public ExperienceSourceManager<RideExperienceSource> newManager() {
        return new Manager();
    }

    @Override
    public boolean matchesParameter(PlayerData player, EntityType obj) {
        if (type == null)
            return true;
        return type.equals(obj);
    }

    private static class Manager extends ExperienceSourceManager<RideExperienceSource> {

        @EventHandler(priority = HIGHEST, ignoreCancelled = true)
        public void onRide(PlayerMoveEvent event) {
            if (!event.getPlayer().isInsideVehicle()) return;

            double deltax = event.getTo().getBlockX() - event.getFrom().getBlockX();
            double deltay = event.getTo().getBlockY() - event.getFrom().getBlockY();
            double deltaz = event.getTo().getBlockZ() - event.getFrom().getBlockZ();
            if (deltax != 0 || deltay != 0 || deltaz != 0) {
                if (!UtilityMethods.isRealPlayer(event.getPlayer())) return;

                PlayerData playerData = PlayerData.get(event.getPlayer());
                Entity vehicle = event.getPlayer().getVehicle();
                for (RideExperienceSource source : getSources()) {
                    if (source.matchesParameter(playerData, vehicle.getType()))
                        source.giveExperience(playerData, event.getFrom().distance(event.getTo()), null);
                }
            }
        }
    }
}
