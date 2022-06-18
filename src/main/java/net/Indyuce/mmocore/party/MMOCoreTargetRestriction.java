package net.Indyuce.mmocore.party;

import io.lumine.mythic.lib.comp.target.InteractionType;
import io.lumine.mythic.lib.comp.target.TargetRestriction;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.guild.AbstractGuild;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class MMOCoreTargetRestriction implements TargetRestriction {

    @Override
    public boolean canTarget(Player player, LivingEntity livingEntity, InteractionType interactionType) {
        if (!interactionType.isOffense() || !(livingEntity instanceof Player))
            return true;

        PlayerData data = PlayerData.get(player);

        // Check for party
        AbstractParty party = MMOCore.plugin.partyModule.getParty(data);
        if (party != null && party.hasMember((Player) livingEntity))
            return false;

        // Check for guild
        AbstractGuild guild = MMOCore.plugin.guildModule.getGuild(data);
        if (guild != null && guild.hasMember((Player) livingEntity))
            return false;

        return true;
    }
}
