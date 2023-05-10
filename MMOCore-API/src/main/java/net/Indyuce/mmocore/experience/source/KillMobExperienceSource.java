package net.Indyuce.mmocore.experience.source;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.api.event.PlayerKillEntityEvent;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import net.Indyuce.mmocore.experience.dispenser.ExperienceDispenser;
import net.Indyuce.mmocore.experience.source.type.SpecificExperienceSource;
import net.Indyuce.mmocore.manager.profession.ExperienceSourceManager;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nullable;

public class KillMobExperienceSource extends SpecificExperienceSource<Entity> {
    private final EntityType type;

    @Nullable
    private final String displayName;

    public KillMobExperienceSource(ExperienceDispenser dispenser, MMOLineConfig config) {
        super(dispenser, config);

        config.validateKeys("type");
        displayName = config.contains("name") ? MythicLib.plugin.parseColors(config.getString("name")) : null;
        type = EntityType.valueOf(config.getString("type").toUpperCase().replace("-", "_").replace(" ", "_"));
    }

    @Override
    public ExperienceSourceManager<KillMobExperienceSource> newManager() {
        return new ExperienceSourceManager<KillMobExperienceSource>() {

            @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
            public void a(PlayerKillEntityEvent event) {
                Bukkit.getScheduler().runTaskLater(MMOCore.plugin, () -> {
                    if (event.getTarget().isDead() && !event.getTarget().getPersistentDataContainer().has(new NamespacedKey(MMOCore.plugin, "spawner_spawned"), PersistentDataType.STRING)) {
                        PlayerData data = PlayerData.get(event.getPlayer());

                        for (KillMobExperienceSource source : getSources())
                            if (source.matches(data, event.getTarget()))
                                source.giveExperience(data, 1, MMOCoreUtils.getCenterLocation(event.getTarget()));
                    }
                }, 2);
            }
        };
    }

    @Override
    public boolean matchesParameter(PlayerData player, Entity obj) {
        return obj.getType() == type && (displayName == null || displayName.equals(obj.getCustomName()));
    }
}
