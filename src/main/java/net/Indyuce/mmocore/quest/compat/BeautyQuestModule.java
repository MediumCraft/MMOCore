package net.Indyuce.mmocore.quest.compat;

import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayerQuestDatas;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.structure.Quest;
import net.Indyuce.mmocore.quest.AbstractQuest;
import org.bukkit.entity.Player;

public class BeautyQuestModule implements QuestModule<BeautyQuestModule.BeautyQuestQuest> {


    @Override
    public BeautyQuestQuest getQuestOrThrow(String questId) {
        Quest quest=QuestsAPI.getQuests().getQuest(Integer.parseInt(questId));
        return quest==null?null:new BeautyQuestQuest(quest);
    }

    @Override
    public boolean hasCompletedQuest(String questId, Player player) {
        PlayerAccount account=PlayersManager.getPlayerAccount(player);
        PlayerQuestDatas quest=account.getQuestDatas(QuestsAPI.getQuests().getQuest(Integer.parseInt(questId)));
        return quest.isFinished();
    }



    public class BeautyQuestQuest implements AbstractQuest {

        Quest quest;

        public BeautyQuestQuest(Quest quest) {
            this.quest = quest;
        }

        @Override
        public String getName() {
            return quest.getName();
        }

        @Override
        public String getId() {
            return ""+quest.getID();
        }
    }
}
