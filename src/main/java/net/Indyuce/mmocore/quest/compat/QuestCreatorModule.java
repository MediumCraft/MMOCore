package net.Indyuce.mmocore.quest.compat;

import com.guillaumevdn.questcreator.ConfigQC;
import com.guillaumevdn.questcreator.data.user.QuestHistoryElement;
import com.guillaumevdn.questcreator.data.user.UserQC;
import com.guillaumevdn.questcreator.lib.model.ElementModel;
import com.guillaumevdn.questcreator.lib.quest.QuestEndType;
import net.Indyuce.mmocore.quest.AbstractQuest;
import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;
import java.util.Arrays;
import java.util.List;

public class QuestCreatorModule implements QuestModule<QuestCreatorModule.QuestCreatorQuest>{

    @Override
    public QuestCreatorQuest getQuestOrThrow(String id) {
        return new QuestCreatorQuest(id);
    }

    @Override
    public boolean hasCompletedQuest(String questId, Player player) {
        UserQC playerData=UserQC.cachedOrNull(player);
        if(playerData==null)
            return false;
        //Gets all the quests the player has  succeeded at
        List<QuestHistoryElement> elements=playerData.getQuestHistory().getElements(questId, Arrays.asList(QuestEndType.SUCCESS),0);
        for(QuestHistoryElement el:elements) {
            if(el.getModelId().equals(questId))
                return true;
        }
        return false;
    }


    /**
     * QC ElementModel corresponds to our quest and their
     * quests correspond to our Quest progress class
     */

    public class QuestCreatorQuest implements AbstractQuest {
        ElementModel questModel;

        public QuestCreatorQuest(String modelId) {
            questModel = ConfigQC.models.getElement(modelId).orNull();
        }

        @Override
        public String getName() {
            return questModel.getDisplayName().getId();
        }

        @Override
        public String getId() {
            return questModel.getId();
        }
    }
}
