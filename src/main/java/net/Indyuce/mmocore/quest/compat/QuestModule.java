package net.Indyuce.mmocore.quest.compat;

import net.Indyuce.mmocore.quest.AbstractQuest;
import org.bukkit.entity.Player;

public interface QuestModule<T extends AbstractQuest> {

    /**
     * @return Quest with given name
     */
    public T getQuestOrThrow(String id);

    /**
     * @return If a specific player did a certain quest
     */
    public boolean hasCompletedQuest(String quest, Player player);

}
