package net.Indyuce.mmocore.comp.mythicmobs.load;

import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.experience.provider.ExperienceDispenser;
import net.Indyuce.mmocore.experience.source.type.SpecificExperienceSource;
import net.Indyuce.mmocore.manager.profession.ExperienceSourceManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class KillMythicFactionExperienceSource extends SpecificExperienceSource<String> {
    private final String factionName;

    public KillMythicFactionExperienceSource(ExperienceDispenser dispenser, MMOLineConfig config) {
        super(dispenser, config);

        config.validate("name");
        factionName = config.getString("name");
    }

    @Override
    public ExperienceSourceManager<KillMythicFactionExperienceSource> newManager() {
        return new ExperienceSourceManager<KillMythicFactionExperienceSource>() {
            @EventHandler
            public void a(MythicMobDeathEvent event) {
                Bukkit.getScheduler().runTaskLater(MMOCore.plugin, () -> {
                    if (!event.getEntity().isDead()) return;
                    if (!event.getMob().hasFaction()) return;
                    if (!(event.getKiller() instanceof Player) || event.getKiller().hasMetadata("NPC")) return;

                    PlayerData data = PlayerData.get((Player) event.getKiller());
                    for (KillMythicFactionExperienceSource source : getSources())
                        if (source.matches(data, event.getMob().getFaction()))
                            source.giveExperience(data, 1, event.getEntity().getLocation());
                }, 2);
            }
        };
    }

    @Override
    public boolean matchesParameter(PlayerData player, String name) {
        return name.equals(factionName);
    }
}
