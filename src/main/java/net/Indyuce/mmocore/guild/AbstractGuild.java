package net.Indyuce.mmocore.guild;

import org.bukkit.entity.Player;

public interface AbstractGuild {

    /**
     * @return If given player is in that party
     */
    boolean hasMember(Player player);
}
