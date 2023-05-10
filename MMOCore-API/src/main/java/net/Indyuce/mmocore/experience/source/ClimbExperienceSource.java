package net.Indyuce.mmocore.experience.source;

import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.experience.dispenser.ExperienceDispenser;
import net.Indyuce.mmocore.experience.source.type.SpecificExperienceSource;
import net.Indyuce.mmocore.manager.profession.ExperienceSourceManager;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

import static org.bukkit.event.EventPriority.HIGHEST;
import static org.bukkit.event.EventPriority.MONITOR;

public class ClimbExperienceSource extends SpecificExperienceSource<Material> {
    //Can be Ladder,Vines,Twisting Vines,Weeping Vines.
    private final Material type;

    /**
     * Gives Experience when a player climbs on a ladder, a vine, a twisting vine or a weeping vine depending
     * on the type precised (if no type is precised it will give xp for the 4)
     * The xp given depends on the vertical distance travelled, the random amount given correspond
     * to the xp when you climb, one block
     */
    public ClimbExperienceSource(ExperienceDispenser dispenser, MMOLineConfig config) {
        super(dispenser, config);
        //If no type precised Ladder and all vines types work
        if (!config.contains("type"))
            type = null;
        else {
            String str = config.getString("type").toUpperCase().replace("-", "_");
            Validate.isTrue(str.equals("LADDER") ||
                            str.equals("VINE") || str.equals("TWISTING_VINES") || str.equals("WEEPING_VINES"),
                    "ClimbExperienceSource problem: The type must be ladder, vine, twisting-vines or weeping-vines");

            type = Material.valueOf(str);
        }


    }


    @Override
    public ExperienceSourceManager<ClimbExperienceSource> newManager() {
        return new ExperienceSourceManager<ClimbExperienceSource>() {
            @EventHandler(priority = HIGHEST,ignoreCancelled = true)
            public void onClimb(PlayerMoveEvent e) {
                double delta=e.getTo().getBlockY()-e.getFrom().getBlockY();
                if (delta > 0) {
                    if (e.getPlayer().hasMetadata("NPC"))
                        return;
                    PlayerData playerData = PlayerData.get(e.getPlayer());
                    for (ClimbExperienceSource source : getSources()) {
                        if (source.matchesParameter(playerData, e.getFrom().getBlock().getType()))
                            source.giveExperience(playerData, delta, null);
                    }
                }
            }
        };
    }

    @Override
    public boolean matchesParameter(PlayerData player, Material material) {
        if (type == null)
            return material.equals(Material.LADDER) || material.equals(Material.VINE) ||
                    material.equals(Material.WEEPING_VINES) || material.equals(Material.TWISTING_VINES) ||
                    material.equals(Material.WEEPING_VINES_PLANT) || material.equals(Material.TWISTING_VINES_PLANT);
        if (type.equals(Material.WEEPING_VINES))
            return material.equals(Material.WEEPING_VINES) || material.equals(Material.WEEPING_VINES_PLANT);
        if (type.equals(Material.TWISTING_VINES))
            return material.equals(Material.TWISTING_VINES) || material.equals(Material.TWISTING_VINES_PLANT);
        return material.equals(type);

    }
}
