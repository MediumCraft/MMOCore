package net.Indyuce.mmocore.experience.source;

import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.experience.dispenser.ExperienceDispenser;
import net.Indyuce.mmocore.experience.source.type.SpecificExperienceSource;
import net.Indyuce.mmocore.manager.profession.ExperienceSourceManager;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

public class EatExperienceSource extends SpecificExperienceSource<ItemStack> {
    private final Material type;

    /**
     * Gives xp when you eat a certain type of food. If not type is given it will give xp from all the food sources.
     * The amount of xp given is the xp per food regenerated.
     */
    public EatExperienceSource(ExperienceDispenser dispenser, MMOLineConfig config) {
        super(dispenser, config);
        if(!config.contains("type"))
            type=null;
        else {
            Material material=Material.valueOf(config.getString("type").toUpperCase().replace("-","_"));
            Validate.isTrue(material!=null,"You must precise a valid material!");
            type=material;
        }
    }

    @Override
    public ExperienceSourceManager<EatExperienceSource> newManager() {
        return new ExperienceSourceManager<EatExperienceSource>() {

            @EventHandler
            public void a(PlayerItemConsumeEvent e) {
                if(!e.getPlayer().hasMetadata("NPC")) {
                    PlayerData playerData = PlayerData.get(e.getPlayer());
                    for (EatExperienceSource source : getSources()) {
                        if (source.matchesParameter(playerData, e.getItem()))
                            source.giveExperience(playerData, 1, null);
                    }
                }
            }
        };
    }

    @Override
    public boolean matchesParameter(PlayerData player, ItemStack obj) {
        if(type==null)
            return true;
        return type.equals(obj.getType());
    }

}
