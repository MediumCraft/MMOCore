package net.Indyuce.mmocore.experience.source;

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

public class RideExperienceSource extends SpecificExperienceSource<Class<? extends Entity>> {
    private final EntityType type;

    /**
     *Gives experience when a player moves riding a certain entity. If no entity type is given it will give xp if you move
     *while riding an entity whatever it is.
     *The random value you give correspond to the xp you get per block travelled while riding.
     */
    public RideExperienceSource(ExperienceDispenser dispenser, MMOLineConfig config) {
        super(dispenser, config);
        if (!config.contains("entity-type"))
            type = null;
        else {
            String str = config.getString("entity-type").toUpperCase().replace("-", "_");
            Validate.isTrue(Arrays.stream(EntityType.values()).map(Objects::toString).collect(Collectors.toList()).contains(str),
                    "The entity-type must correspond to an entity that exist in the game.");
            type=EntityType.valueOf(str);
        }

    }

    @Override
    public ExperienceSourceManager<RideExperienceSource> newManager() {
        return new ExperienceSourceManager<RideExperienceSource>() {
            @EventHandler
            public void onRide(PlayerMoveEvent e) {
                if (e.getPlayer().hasMetadata("NPC"))
                    return;
                PlayerData playerData=PlayerData.get(e.getPlayer());
                if(e.getPlayer().isInsideVehicle()) {
                    Entity vehicle=e.getPlayer().getVehicle();
                    for(RideExperienceSource source:getSources()) {
                        if(source.matchesParameter(playerData,vehicle.getClass()))
                            giveExperience(playerData,e.getFrom().distance(e.getTo()),null);
                    }
                }
            }
        };
    }

    @Override
    public boolean matchesParameter(PlayerData player, Class<? extends Entity> obj) {
        if(type==null)
            return true;
        return type.getEntityClass().isAssignableFrom(obj);
    }

}
