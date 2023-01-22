package net.Indyuce.mmocore.guild;

import io.lumine.mythic.lib.comp.interaction.relation.Relationship;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public interface GuildModule {

    @Nullable
    public AbstractGuild getGuild(PlayerData playerData);

    public Relationship getRelationship(Player player, Player target);
}
