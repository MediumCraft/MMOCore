package net.Indyuce.mmocore.party;

import io.lumine.mythic.lib.comp.interaction.relation.Relationship;
import io.lumine.mythic.lib.comp.interaction.relation.RelationshipHandler;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PartyRelationHandler implements RelationshipHandler {

    @NotNull
    @Override
    public Relationship getRelationship(Player player, Player target) {
        final AbstractParty party = MMOCore.plugin.partyModule.getParty(PlayerData.get(player));
        return party != null && party.hasMember(target) ? Relationship.PARTY_MEMBER : Relationship.PARTY_OTHER;
    }
}
