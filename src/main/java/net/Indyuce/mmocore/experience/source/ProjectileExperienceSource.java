package net.Indyuce.mmocore.experience.source;

import de.schlichtherle.key.passwd.swing.BasicUnknownKeyFeedback;
import io.lumine.mythic.core.skills.mechanics.ShootMechanic;
import io.lumine.mythic.lib.api.MMOLineConfig;
import me.glaremasters.guilds.utils.BackupUtils;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.experience.dispenser.ExperienceDispenser;
import net.Indyuce.mmocore.experience.source.type.SpecificExperienceSource;
import net.Indyuce.mmocore.manager.profession.ExperienceSourceManager;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ProjectileExperienceSource extends SpecificExperienceSource<Projectile> {

    private final ProjectileType projectileType;

    public ProjectileExperienceSource(ExperienceDispenser dispenser, MMOLineConfig config) {
        super(dispenser, config);
        if(!config.contains("type"))
            projectileType=null;
        else {
            String str=config.getString("type").toUpperCase().replace("-", "_");
            Validate.isTrue(Arrays.stream(ProjectileType.values()).map(ProjectileType::toString).collect(Collectors.toList()).contains(str));
            projectileType = ProjectileType.valueOf(str);
        }
    }

    @Override
    public ExperienceSourceManager<ProjectileExperienceSource> newManager() {

        return new ExperienceSourceManager<ProjectileExperienceSource>() {
            HashMap<Projectile, Location> projectiles = new HashMap<>();

            @EventHandler
            public void onHit(EntityDamageByEntityEvent e) {

                if (e.getDamager() instanceof Projectile) {
                    Projectile projectile = (Projectile) e.getDamager();
                    if (projectile.getShooter() instanceof Player && !((Player) projectile.getShooter()).hasMetadata("NPC")) {
                        Player player = (Player) projectile.getShooter();
                        PlayerData playerData = PlayerData.get(player);
                        Validate.isTrue(projectiles.containsKey(projectile));
                        double distance = projectiles.get(projectile).distance(e.getDamager().getLocation());
                        for (ProjectileExperienceSource source : getSources()) {
                            if (source.matchesParameter(playerData, projectile))
                                source.giveExperience(playerData, e.getFinalDamage() * distance, null);
                        }


                    }
                }

            }

            //Mark every arrow with the the location at which it was shot to calculate the distance
            @EventHandler
            public void onLaunch(ProjectileLaunchEvent e) {
                if (e.getEntity().getShooter() instanceof Player) {
                    Player player = (Player) e.getEntity().getShooter();
                    if (player.hasMetadata("NPC"))
                        return;


                    projectiles.put(e.getEntity(), e.getLocation());
                    //Remove the projectile 15 s after it was launched
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            projectiles.remove(e.getEntity());
                        }
                    }.runTaskLater(MMOCore.plugin, 15 * 20L);


                }
            }

        };
    }

    @Override
    public boolean matchesParameter(PlayerData player, Projectile projectile) {
        if(projectileType==null)
            return true;
        return projectileType.matches(projectile);
    }


    public enum ProjectileType {
        ARROW((p)-> p instanceof Arrow),
        TRIDENT((p)-> p instanceof Trident),
        FIREBALL((p)-> p instanceof Fireball),
        FISH_HOOK((p)-> p instanceof FishHook),
        ;

        private final Function<Projectile,Boolean> matching;

        ProjectileType(Function<Projectile,Boolean> matching) {
            this.matching=matching;
        }


        public boolean matches(Projectile projectile) {
            return matching.apply(projectile);
        }
    }

}
