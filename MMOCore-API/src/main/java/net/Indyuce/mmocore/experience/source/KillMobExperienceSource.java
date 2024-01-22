package net.Indyuce.mmocore.experience.source;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.util.FlushableRegistry;
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
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nullable;
import java.util.UUID;

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

            /**
             * This map is used to keep track of the last player who
             * hit some entity. It is flushed on entity death.
             */
            private final FlushableRegistry<UUID, UUID> registry = new FlushableRegistry<>((entity, attacker) -> Bukkit.getEntity(entity) == null, 20 * 60);

            @Override
            public void whenClosed() {
                registry.close();
            }

            @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
            public void registerLastAttacker(PlayerAttackEvent event) {
                registry.getRegistry().put(event.getEntity().getUniqueId(), event.getAttacker().getPlayer().getUniqueId());
            }

            @EventHandler(priority = EventPriority.MONITOR)
            public void giveExp(EntityDeathEvent event) {

                // Always remove entry from map
                final @Nullable UUID lastAttacker = this.registry.getRegistry().remove(event.getEntity().getUniqueId());
                if (lastAttacker == null) return;

                if (event.getEntity().getPersistentDataContainer().has(new NamespacedKey(MMOCore.plugin, "spawner_spawned"), PersistentDataType.STRING))
                    return;

                final PlayerData data = PlayerData.get(lastAttacker);
                for (KillMobExperienceSource source : getSources())
                    if (source.matches(data, event.getEntity()))
                        source.giveExperience(data, 1, MMOCoreUtils.getCenterLocation(event.getEntity()));
            }
        };
    }

    @Override
    public boolean matchesParameter(PlayerData player, Entity obj) {
        return obj.getType() == type && (displayName == null || displayName.equals(obj.getCustomName()));
    }
}
