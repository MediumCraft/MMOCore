package net.Indyuce.mmocore.experience.source;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.util.Lazy;
import io.lumine.mythic.lib.version.Attributes;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.experience.dispenser.ExperienceDispenser;
import net.Indyuce.mmocore.experience.source.type.SpecificExperienceSource;
import net.Indyuce.mmocore.manager.profession.ExperienceSourceManager;
import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.bukkit.event.EventPriority.HIGHEST;

public class DamageTakenExperienceSource extends SpecificExperienceSource<EntityDamageEvent.DamageCause> {
    private final EntityDamageEvent.DamageCause cause;

    /**
     * Gives experience when a player takes damage of a certain type. If no cause is given it will give xp for all
     * the damage causes. The random value you give correspond to the xp you get per damage taken.
     */
    public DamageTakenExperienceSource(ExperienceDispenser dispenser, MMOLineConfig config) {
        super(dispenser, config);
        if (!config.contains("type"))
            cause = null;
        else {
            String str = config.getString("type").toUpperCase().replace("-", "_");
            //Checks if the damage type correspond to a value of the damage type enum
            Validate.isTrue(Arrays.stream(EntityDamageEvent.DamageCause.values()).map(Objects::toString).collect(Collectors.toList()).contains(str),
                    "Cause not allowed. Go check at all the Damage Causes in EntityDamageEvent.DamageCause enum.");
            cause = EntityDamageEvent.DamageCause.valueOf(str);
        }
    }

    @Override
    public ExperienceSourceManager<DamageTakenExperienceSource> newManager() {
        return new Manager();
    }

    @Override
    public boolean matchesParameter(PlayerData player, EntityDamageEvent.DamageCause damageCause) {
        if (player.getPlayer().isDead()) return false;
        return cause == null || damageCause.equals(cause);
    }

    private static class Manager extends ExperienceSourceManager<DamageTakenExperienceSource> {

        @EventHandler(priority = HIGHEST, ignoreCancelled = true)

        public void onDamageTaken(EntityDamageEvent event) {
            if (!UtilityMethods.isRealPlayer(event.getEntity())) return;

            final PlayerData playerData = PlayerData.get((Player) event.getEntity());
            final Lazy<Double> effectiveDamage = Lazy.of(() -> {
                final double eventDamage = event.getDamage();
                final double maxHealth = ((Player) event.getEntity()).getAttribute(Attributes.MAX_HEALTH).getValue();
                return Math.min(eventDamage, maxHealth);
            });

            // Wait 2 tick to check if the player died
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (DamageTakenExperienceSource source : getSources())
                        if (source.matchesParameter(playerData, event.getCause())) {
                            //  System.out.println("-> " + effectiveDamage.get());
                            source.giveExperience(playerData, effectiveDamage.get(), null);
                        }
                }
            }.runTaskLater(MMOCore.plugin, 2);
        }
    }
}
