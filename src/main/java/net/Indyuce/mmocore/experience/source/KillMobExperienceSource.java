package net.Indyuce.mmocore.experience.source;

import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.api.event.EntityKillEntityEvent;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.experience.provider.ExperienceDispenser;
import net.Indyuce.mmocore.experience.source.type.SpecificExperienceSource;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.manager.profession.ExperienceSourceManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class KillMobExperienceSource extends SpecificExperienceSource<Entity> {
    public final EntityType type;

    public KillMobExperienceSource(ExperienceDispenser dispenser, MMOLineConfig config) {
        super(dispenser, config);

        config.validate("type");
        type = EntityType.valueOf(config.getString("type").toUpperCase().replace("-", "_").replace(" ", "_"));
    }

    @Override
    public ExperienceSourceManager<KillMobExperienceSource> newManager() {
        return new ExperienceSourceManager<KillMobExperienceSource>() {
            @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
            public void a(EntityKillEntityEvent event) {
                Bukkit.getScheduler().runTaskLater(MMOCore.plugin, () -> {
                    if (event.getTarget().isDead() && event.getEntity() instanceof Player && !event.getEntity().hasMetadata("NPC")
                            && !event.getTarget().hasMetadata("spawner_spawned")) {
                        PlayerData data = PlayerData.get((Player) event.getEntity());

                        for (KillMobExperienceSource source : getSources())
                            if (source.matches(data, event.getTarget()))
                                source.giveExperience(data, 1, event.getTarget().getLocation());
                    }
                }, 2);
            }
        };
    }

    @Override
    public boolean matchesParameter(PlayerData player, Entity obj) {
        return obj.getType() == type;
    }
}
