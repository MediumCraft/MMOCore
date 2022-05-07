package net.Indyuce.mmocore.experience.source;

import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import net.Indyuce.mmocore.experience.dispenser.ExperienceDispenser;
import net.Indyuce.mmocore.experience.source.type.SpecificExperienceSource;
import net.Indyuce.mmocore.manager.profession.ExperienceSourceManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import javax.swing.text.html.parser.Entity;

public class TamingExperienceSource extends SpecificExperienceSource {


    public TamingExperienceSource(ExperienceDispenser dispenser, MMOLineConfig config) {
        super(dispenser, config);
    }

    @Override
    public ExperienceSourceManager<TamingExperienceSource> newManager() {
        return new ExperienceSourceManager<TamingExperienceSource>() {
            @EventHandler
            public void onWolfHit(EntityDamageByEntityEvent e) {
                if(e.getDamager() instanceof Wolf) {
                    Wolf wolf= (Wolf) e.getDamager();
                    if(wolf.getOwner() instanceof Player &&!((Player) wolf.getOwner()).hasMetadata("NPC")) {
                        PlayerData playerData=PlayerData.get((OfflinePlayer) wolf.getOwner());
                        for(TamingExperienceSource source:getSources()) {
                            source.giveExperience(playerData,e.getDamage(), MMOCoreUtils.getCenterLocation(e.getEntity()));
                        }
                    }
                }
            }
        };
    }

    @Override
    public boolean matchesParameter(PlayerData player, Object obj) {
        return false;
    }
}
