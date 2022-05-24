package net.Indyuce.mmocore.quest;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.quest.Quest;
import net.Indyuce.mmocore.quest.compat.QuestModule;
import org.bukkit.entity.Player;

public class MMOCoreQuestModule implements QuestModule {

    @Override
    public AbstractQuest getQuest(String id) {
        Quest quest=MMOCore.plugin.questManager.get(id);
        if(quest==null)
            return null;
        return new MMOCoreQuest(quest);
    }

    @Override
    public boolean hasCompletedQuest(String quest, Player player) {
        return false;
    }

    public class MMOCoreQuest implements AbstractQuest {
        Quest quest;

        public MMOCoreQuest(Quest quest) {

            this.quest = quest;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public String getId() {
            return null;
        }
    }
}
