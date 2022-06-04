package net.Indyuce.mmocore.party;

import io.lumine.mythic.lib.comp.target.InteractionType;
import io.lumine.mythic.lib.comp.target.TargetRestriction;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class MMOCoreTargetRestriction implements TargetRestriction {

    @Override
    public boolean canTarget(Player player, LivingEntity livingEntity, InteractionType interactionType) {
        if (!interactionType.isOffense() || !(livingEntity instanceof Player) || livingEntity.hasMetadata("NPC"))
            return true;

        AbstractParty party = MMOCore.plugin.partyModule.getParty(PlayerData.get(player));
        // TODO check for guild
        return party == null || !party.hasMember((Player) livingEntity);
    }
}
