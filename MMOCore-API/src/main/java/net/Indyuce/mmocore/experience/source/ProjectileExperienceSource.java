package net.Indyuce.mmocore.experience.source;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.util.FlushableRegistry;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.experience.dispenser.ExperienceDispenser;
import net.Indyuce.mmocore.experience.source.type.SpecificExperienceSource;
import net.Indyuce.mmocore.manager.profession.ExperienceSourceManager;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.bukkit.event.EventPriority.HIGHEST;

public class ProjectileExperienceSource extends SpecificExperienceSource<Projectile> {
    private final ProjectileType projectileType;

    public ProjectileExperienceSource(ExperienceDispenser dispenser, MMOLineConfig config) {
        super(dispenser, config);
        if (!config.contains("type")) projectileType = null;
        else {
            String str = config.getString("type").toUpperCase().replace("-", "_");
            Validate.isTrue(Arrays.stream(ProjectileType.values()).map(ProjectileType::toString).collect(Collectors.toList()).contains(str));
            projectileType = ProjectileType.valueOf(str);
        }
    }

    @Override
    public ExperienceSourceManager<ProjectileExperienceSource> newManager() {
        return new Manager();
    }

    @Override
    public boolean matchesParameter(PlayerData player, Projectile projectile) {
        if (projectileType == null) return true;
        return projectileType.matches(projectile);
    }

    public enum ProjectileType {
        ARROW((p) -> p instanceof Arrow), TRIDENT((p) -> p instanceof Trident);

        private final Function<Projectile, Boolean> matching;

        ProjectileType(Function<Projectile, Boolean> matching) {
            this.matching = matching;
        }


        public boolean matches(Projectile projectile) {
            return matching.apply(projectile);
        }
    }

    private static class Manager extends ExperienceSourceManager<ProjectileExperienceSource> {
        private final FlushableRegistry<Projectile, Location> projectiles = new FlushableRegistry<>((proj, loc) -> proj.isDead(), 20 * 60);

        @EventHandler(priority = HIGHEST, ignoreCancelled = true)
        public void onHit(ProjectileHitEvent e) {
            if (e.getHitBlock() != null) projectiles.getRegistry().remove(e.getEntity());
        }

        @EventHandler(priority = HIGHEST, ignoreCancelled = true)
        public void onDamage(EntityDamageByEntityEvent e) {

            if (e.getEntity() instanceof Projectile) {
                Projectile projectile = (Projectile) e.getEntity();
                Location loc = projectiles.getRegistry().get(projectile);
                if (loc == null) return;

                if (projectile.getShooter() instanceof Player && !((Player) projectile.getShooter()).hasMetadata("NPC")) {
                    Player player = (Player) projectile.getShooter();
                    PlayerData playerData = PlayerData.get(player);
                    double distance = loc.distance(e.getEntity().getLocation());
                    for (ProjectileExperienceSource source : getSources()) {
                        if (source.matchesParameter(playerData, projectile))
                            source.giveExperience(playerData, e.getFinalDamage() * distance, null);
                    }
                }
            }

        }

        // Mark every arrow with the location at which it was shot to calculate the distance
        @EventHandler
        public void onLaunch(ProjectileLaunchEvent e) {
            if (e.getEntity().getShooter() instanceof Player && UtilityMethods.isRealPlayer((Player) e.getEntity().getShooter())) {
                projectiles.getRegistry().put(e.getEntity(), e.getLocation());
            }
        }
    }
}
