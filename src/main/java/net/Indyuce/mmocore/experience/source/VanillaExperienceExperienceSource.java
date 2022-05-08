package net.Indyuce.mmocore.experience.source;

import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.experience.dispenser.ExperienceDispenser;
import net.Indyuce.mmocore.experience.source.type.SpecificExperienceSource;
import net.Indyuce.mmocore.manager.profession.ExperienceSourceManager;
import org.apache.commons.lang.Validate;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerExpChangeEvent;

public class VanillaExperienceExperienceSource extends SpecificExperienceSource {


    public VanillaExperienceExperienceSource(ExperienceDispenser dispenser, MMOLineConfig config) {
        super(dispenser, config);
    }

    @Override
    public ExperienceSourceManager<VanillaExperienceExperienceSource> newManager() {
        return new ExperienceSourceManager<VanillaExperienceExperienceSource>() {
            @EventHandler
            public void onExp(PlayerExpChangeEvent e) {
                if(e.getPlayer().hasMetadata("NPC"))
                    return;
                PlayerData playerData=PlayerData.get(e.getPlayer());
                for(VanillaExperienceExperienceSource source:getSources()) {
                    if(source.matchesParameter(playerData,null))
                        giveExperience(playerData,e.getAmount(),null);
                }
            }
        };
    }

    @Override
    public boolean matchesParameter(PlayerData player, Object obj) {
        return true;
    }
}
