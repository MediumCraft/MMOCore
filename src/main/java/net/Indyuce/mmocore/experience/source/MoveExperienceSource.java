package net.Indyuce.mmocore.experience.source;

import com.mojang.datafixers.types.Func;
import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.damage.DamageType;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.experience.dispenser.ExperienceDispenser;
import net.Indyuce.mmocore.experience.source.type.SpecificExperienceSource;
import net.Indyuce.mmocore.manager.profession.ExperienceSourceManager;
import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.xml.sax.SAXParseException;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MoveExperienceSource extends SpecificExperienceSource {
    MovingType type;



    public MoveExperienceSource(ExperienceDispenser dispenser, MMOLineConfig config) {
        super(dispenser, config);
        if (!config.contains("moving-type"))
            type = null;
        else {
            String str = config.getString("moving-type").toUpperCase().replace("-", "_");
            //Checks if the damage type correspond to a value of the damage type enum
            Validate.isTrue(Arrays.stream(MoveExperienceSource.MovingType.values()).map(Objects::toString).collect(Collectors.toList()).contains(str),
                    "moving-type value not allowed. Moving type values allowed: sneaking, flying, swimming, sprinting, walking.");
            type = MovingType.valueOf(str);
        }
    }

    @Override
    public ExperienceSourceManager<MoveExperienceSource> newManager() {
        return new ExperienceSourceManager<MoveExperienceSource>() {
            @EventHandler
            public void onMove(PlayerMoveEvent e) {
                if(e.getPlayer().hasMetadata("NPC"))
                    return;
                Player player=e.getPlayer();
                PlayerData playerData =PlayerData.get(player);
                for(MoveExperienceSource source:getSources()) {
                    if(source.matchesParameter(playerData,null)) {
                        giveExperience(playerData,e.getTo().distance(e.getFrom()),null);
                    }
                }
            }
        };
    }

    @Override
    public boolean matchesParameter(PlayerData player, Object obj) {
        return type.matches(player.getPlayer());
    }

    public enum MovingType {
        SNEAKING(Player::isSneaking),
        FLYING(Player::isFlying),
        SWIMMING(Player::isSwimming),
        SPRINTING(Player::isSprinting),
        WALKING((p) -> !p.isSneaking() && !p.isSprinting() && !p.isFlying() && !p.isSwimming());

        private final Function<Player, Boolean> matching;

        MovingType(Function<Player, Boolean> matching) {
            this.matching = matching;
        }

        public boolean matches(Player player) {
            return matching.apply(player);
        }

    }
}
