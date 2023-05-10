package net.Indyuce.mmocore.guild;

import io.lumine.mythic.lib.comp.interaction.relation.Relationship;
import io.lumine.mythic.lib.comp.interaction.relation.RelationshipHandler;
import net.Indyuce.mmocore.MMOCore;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GuildRelationHandler implements RelationshipHandler {

    @NotNull
    @Override
    public Relationship getRelationship(Player player, Player target) {
        return MMOCore.plugin.guildModule.getRelationship(player, target);
    }
}
