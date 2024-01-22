package net.Indyuce.mmocore.experience.source;

import io.lumine.mythic.lib.UtilityMethods;
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

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.bukkit.event.EventPriority.MONITOR;

public class DamageDealtExperienceSource extends SpecificExperienceSource<DamageType> {
    private final DamageType type;

    /**
     * Gives experience when a player deals damage of a certain type. If no type is given it will give xp for all
     * the damage type. The random value you give correspond to the xp you get per damage dealt.
     */

    public DamageDealtExperienceSource(ExperienceDispenser dispenser, MMOLineConfig config) {
        super(dispenser, config);
        if (!config.contains("type"))
            type = null;
        else {
            String str = UtilityMethods.enumName(config.getString("type"));
            //Checks if the damage type correspond to a value of the damage type enum
            Validate.isTrue(Arrays.stream(DamageType.values()).map(Objects::toString).collect(Collectors.toList()).contains(str),
                    "Type value not allowed. Type value allowed: magic, physical, weapon, skill, projectile," +
                            " unarmed, on-hit, minion, dot.");
            type = DamageType.valueOf(str);
        }
    }

    @Override
    public ExperienceSourceManager<DamageDealtExperienceSource> newManager() {
        return new ExperienceSourceManager<DamageDealtExperienceSource>() {
            //It isn't triggered when the PlayerAttackEvent gets cancelled
            @EventHandler(priority = MONITOR,ignoreCancelled = true)
            public void onDamageDealt(PlayerAttackEvent e) {
                PlayerData playerData = PlayerData.get(e.getPlayer());
                for (DamageDealtExperienceSource source : getSources()) {
                    double value = 0;
                    for (DamagePacket packet : e.getDamage().getPackets()) {
                        for (DamageType damageType : packet.getTypes()) {
                            if (source.matchesParameter(playerData, damageType))
                                value += packet.getFinalValue();
                        }

                    }
                    source.giveExperience(playerData, value, null);
                }

            }
        };
    }

    @Override
    public boolean matchesParameter(PlayerData player, DamageType damageType) {
        if (type == null) {
            return true;
        }
        else {
            return type.equals(damageType);

        }
    }

}
