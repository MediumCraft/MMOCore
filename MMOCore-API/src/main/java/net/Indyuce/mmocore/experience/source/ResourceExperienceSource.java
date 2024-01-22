package net.Indyuce.mmocore.experience.source;

import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.resource.PlayerResource;
import net.Indyuce.mmocore.experience.dispenser.ExperienceDispenser;
import net.Indyuce.mmocore.experience.source.type.SpecificExperienceSource;
import net.Indyuce.mmocore.manager.profession.ExperienceSourceManager;
import net.Indyuce.mmocore.api.event.PlayerResourceUpdateEvent;
import org.apache.commons.lang.Validate;
import org.bukkit.event.EventHandler;

import static org.bukkit.event.EventPriority.HIGHEST;

public class ResourceExperienceSource extends SpecificExperienceSource<PlayerResource> {
    private final PlayerResource resource;

    /**
     * Gives experience when the player uses a specific resource type. If no type is precised it will trigger for
     * mana, stamina and stellium. The amount specified si the xp given per resource consummed.
     */
    public ResourceExperienceSource(ExperienceDispenser dispenser, MMOLineConfig config) {
        super(dispenser, config);
        if (!config.contains("type"))
            resource = null;
        else {
            String str = config.getString("type").toUpperCase().replace("-", "_");
            Validate.isTrue(str.equals("MANA") || str.equals("STELLIUM") || str.equals("STAMINA"),
                    "ResourceExperienceSource problem: The resource can only be mana, stamina or STELLIUM");
            resource = PlayerResource.valueOf(str);
        }


    }

    @Override
    public ExperienceSourceManager<ResourceExperienceSource> newManager() {
        return new ExperienceSourceManager<ResourceExperienceSource>() {
            @EventHandler(priority = HIGHEST,ignoreCancelled = true)
            public void onResource(PlayerResourceUpdateEvent e) {
                if (e.getPlayer().hasMetadata("NPC"))
                    return;
                PlayerData playerData = PlayerData.get(e.getPlayer());
                if(e.getAmount()<0)
                for (ResourceExperienceSource source : getSources()) {
                    if (source.matchesParameter(playerData, e.getResource()))
                        source.giveExperience(playerData, -e.getAmount(), null);
                }
            }
        };
    }

    @Override
    public boolean matchesParameter(PlayerData player, PlayerResource obj) {
        if (resource == null)
            return !obj.equals(PlayerResource.HEALTH);
        return resource.equals(obj);
    }
}
