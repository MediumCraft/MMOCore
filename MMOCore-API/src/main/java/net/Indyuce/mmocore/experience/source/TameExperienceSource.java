package net.Indyuce.mmocore.experience.source;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import net.Indyuce.mmocore.experience.dispenser.ExperienceDispenser;
import net.Indyuce.mmocore.experience.source.type.SpecificExperienceSource;
import net.Indyuce.mmocore.manager.profession.ExperienceSourceManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityTameEvent;

public class TameExperienceSource extends SpecificExperienceSource<EntityType> {
    public TameExperienceSource(ExperienceDispenser dispenser, MMOLineConfig config) {
        super(dispenser, config);
    }

    @Override
    public ExperienceSourceManager<TameExperienceSource> newManager() {
        return new Manager();
    }

    @Override
    public boolean matchesParameter(PlayerData player, EntityType entityType) {
        return true;
    }

    private static class Manager extends ExperienceSourceManager<TameExperienceSource> {

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onWolfHit(EntityTameEvent event) {

            // Only wolves at the moment
            if (!(event.getEntity() instanceof Wolf)) return;

            if (!(event.getOwner() instanceof Player) || !UtilityMethods.isRealPlayer((Entity) event.getOwner()))
                return;

            PlayerData playerData = PlayerData.get((OfflinePlayer) event.getOwner());
            for (TameExperienceSource source : getSources())
                if (source.matches(playerData, event.getEntity().getType()))
                    source.giveExperience(playerData, 1, MMOCoreUtils.getCenterLocation(event.getEntity()));
        }
    }
}
