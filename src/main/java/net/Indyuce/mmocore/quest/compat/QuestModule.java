package net.Indyuce.mmocore.quest.compat;

import net.Indyuce.mmocore.quest.AbstractQuest;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;

public interface QuestModule<T extends AbstractQuest> {

    /**
     * @return Quest with given name
     */
    @Nullable
    public T getQuest(String id);

    /**
     * @return If a specific player did a certain quest
     */
    public boolean hasCompletedQuest(String quest, Player player);

}
