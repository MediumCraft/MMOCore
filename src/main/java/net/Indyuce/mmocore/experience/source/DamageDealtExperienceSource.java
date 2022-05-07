package net.Indyuce.mmocore.experience.source;

import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.damage.DamagePacket;
import io.lumine.mythic.lib.damage.DamageType;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.experience.dispenser.ExperienceDispenser;
import net.Indyuce.mmocore.experience.source.type.SpecificExperienceSource;
import net.Indyuce.mmocore.manager.profession.ExperienceSourceManager;
import org.apache.commons.lang.Validate;
import org.bukkit.event.EventHandler;
import scala.Enumeration;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class DamageDealtExperienceSource extends SpecificExperienceSource<DamageType> {
    private final DamageType type;

    /**
     *Gives experience when a player deals damage of a certain type. If no type is given it will give xp for all
     * the damage type. The random value you give correspond to the xp you get per damage dealt.
     */

    public DamageDealtExperienceSource(ExperienceDispenser dispenser, MMOLineConfig config) {
        super(dispenser, config);
        if (!config.contains("damage-type"))
            type = null;
        else {
            String str = config.getString("damage-type").toUpperCase().replace("-", "_");
            //Checks if the damage type correspond to a value of the damage type enum
            Validate.isTrue(Arrays.stream(DamageType.values()).map(Objects::toString).collect(Collectors.toList()).contains(str),
                    "damage-type value not allowed. Damage type value allowed: magic, physical, weapon, skill, projectile," +
                            " unarmed, on-hit, minion, dot.");
            type = DamageType.valueOf(str);

        }}

    @Override
    public ExperienceSourceManager<DamageDealtExperienceSource> newManager() {
        return new ExperienceSourceManager<DamageDealtExperienceSource>() {
            @EventHandler
            public void onDamageDealt(PlayerAttackEvent e) {
                if(e.getPlayer().hasMetadata("NPC"))
                    return;
                PlayerData playerData=PlayerData.get(e.getPlayer());
                for(DamagePacket packet:e.getDamage().getPackets()) {
                    for(DamageType damageType:packet.getTypes()) {

                        for(DamageDealtExperienceSource source: getSources()) {
                            if(source.matchesParameter(playerData,damageType))
                                source.giveExperience(playerData, packet.getFinalValue(), null);
                        }
                    }

                }

            }
        };
    }

    @Override
    public boolean matchesParameter(PlayerData player, DamageType damageType) {
        if(type==null)
            return true;
        else
            return type.equals(damageType);
    }

}
