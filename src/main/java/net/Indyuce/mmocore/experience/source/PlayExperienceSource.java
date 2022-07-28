package net.Indyuce.mmocore.experience.source;

import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.experience.dispenser.ExperienceDispenser;
import net.Indyuce.mmocore.experience.source.type.SpecificExperienceSource;
import net.Indyuce.mmocore.manager.profession.ExperienceSourceManager;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;

public class PlayExperienceSource extends SpecificExperienceSource {

    private final World world;
    private final double x1, x2, z1, z2;
    private final boolean inCombat;

    /**
     * Experience source giving the specified amount of xp to all the players online each second in certain world bounds.
     * If no bounds are given, it will give the xp to every player online. You can also specifiy if the player
     * has to be inCombat or not to get the xp.
     */
    public PlayExperienceSource(ExperienceDispenser dispenser, MMOLineConfig config) {
        super(dispenser, config);

        inCombat = config.getBoolean("in-combat", false);
        world = config.contains("world") ? Objects.requireNonNull(Bukkit.getWorld(config.getString("world")), "Could not find world " + config.getString("world")) : null;
        if (!config.contains("x1") || !config.contains("x2")) {
            x1 = Double.NEGATIVE_INFINITY;
            x2 = Double.POSITIVE_INFINITY;
        } else {
            x1 = Math.min(config.getInt("x1"), config.getInt("x2"));
            x2 = Math.max(config.getInt("x1"), config.getInt("x2"));
        }
        if (!config.contains("z1") || !config.contains("z2")) {
            z1 = Double.NEGATIVE_INFINITY;
            z2 = Double.POSITIVE_INFINITY;
        } else {
            z1 = Math.min(config.getInt("z1"), config.getInt("z2"));
            z2 = Math.max(config.getInt("z1"), config.getInt("z2"));
        }
    }

    @Override
    public ExperienceSourceManager<PlayExperienceSource> newManager() {
        return new PlayingExperienceSourceManager();

    }

    @Override
    public boolean matchesParameter(PlayerData player, Object obj) {
        if (inCombat && !player.isInCombat())
            return false;

        if (world == null)
            return true;
        Location location = player.getPlayer().getLocation();
        return location.getWorld().equals(world) && location.getX() > x1 && location.getX() < x2
                && location.getZ() > z1 && location.getZ() < z2;
    }


    private class PlayingExperienceSourceManager extends ExperienceSourceManager<PlayExperienceSource> {

        public PlayingExperienceSourceManager() {
            new BukkitRunnable() {

                @Override
                public void run() {
                    Bukkit.getOnlinePlayers().forEach((player) -> {
                        if (!player.hasMetadata("NPC")) {
                            PlayerData playerData = PlayerData.get(player);
                            for (PlayExperienceSource source : getSources()) {
                                if (source.matchesParameter(playerData, null))
                                    giveExperience(playerData, 1, null);
                            }
                        }
                    });
                }

            }.runTaskTimer(MMOCore.plugin, 0, 20);
        }
    }
}
