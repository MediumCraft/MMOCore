package net.Indyuce.mmocore.quest.compat;

import net.Indyuce.mmocore.quest.AbstractQuest;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;

public interface QuestModule<T extends AbstractQuest> {

    /**
     * @return Quest with given identifier
     */
    @Nullable
    public T getQuestOrThrow(String id);

    /**
     * @return If a specific player has made a certain quest
     */
    public boolean hasCompletedQuest(String quest, Player player);
}
