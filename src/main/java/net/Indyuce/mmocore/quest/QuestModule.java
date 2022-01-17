package net.Indyuce.mmocore.quest;

import net.Indyuce.mmocore.api.player.PlayerData;

public interface QuestModule<T extends AbstractQuest, U extends PlayerQuestData<T>> {

    /**
     * @return Quest with given name
     */
    public T getQuestOrThrow(String id);

    /**
     * @return Info about the completed quests from a specific player
     */
    public U getQuestData(PlayerData playerData);
}
