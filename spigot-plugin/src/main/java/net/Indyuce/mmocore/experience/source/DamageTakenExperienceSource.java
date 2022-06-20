package net.Indyuce.mmocore.experience.source;

import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.experience.dispenser.ExperienceDispenser;
import net.Indyuce.mmocore.experience.source.type.SpecificExperienceSource;
import net.Indyuce.mmocore.manager.profession.ExperienceSourceManager;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.apache.commons.lang.Validate;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class DamageTakenExperienceSource extends SpecificExperienceSource<EntityDamageEvent.DamageCause> {
    private final EntityDamageEvent.DamageCause cause;

    /**
     * Gives experience when a player takes damage from a certain cause. If no cause is given it will give xp for all
     * the damage causes. The random value you give correspond to the xp you get per damage taken.
     */
    public DamageTakenExperienceSource(ExperienceDispenser dispenser, MMOLineConfig config) {
        super(dispenser, config);
        if (!config.contains("cause"))
            cause = null;
        else {
            String str = config.getString("cause").toUpperCase().replace("-", "_");
            //Checks if the damage type correspond to a value of the damage type enum
            Validate.isTrue(Arrays.stream(EntityDamageEvent.DamageCause.values()).map(Objects::toString).collect(Collectors.toList()).contains(str),
                    "Cause not allowed. Go check at all the Damage Causes in EntityDamageEvent.DamageCause enum.");
            cause = EntityDamageEvent.DamageCause.valueOf(str);
        }
    }

    @Override
    public ExperienceSourceManager<DamageTakenExperienceSource> newManager() {
        return new ExperienceSourceManager<DamageTakenExperienceSource>() {
            @EventHandler
            public void onDamageTaken(EntityDamageEvent e) {
                if (e.getEntity() instanceof Player && !e.getEntity().hasMetadata("NPC")) {
                    double amount = e.getDamage();
                    PlayerData playerData = PlayerData.get((OfflinePlayer) e.getEntity());
                    //We wait 2 tick to check if the player is Dead
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            for (DamageTakenExperienceSource source : getSources()) {
                                if (source.matchesParameter(playerData, e.getCause()))
                                    source.giveExperience(playerData, amount, null);
                            }
                        }
                    }.runTaskLater(MMOCore.plugin, 2);
                }
            }
        };
    }

    @Override
    public boolean matchesParameter(PlayerData player, EntityDamageEvent.DamageCause damageCause) {
        if (player.getPlayer().isDead())
            return false;
        if (cause == null)
            return true;
        return damageCause.equals(cause);
    }
}
