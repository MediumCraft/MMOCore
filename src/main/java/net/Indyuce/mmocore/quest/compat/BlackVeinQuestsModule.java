package net.Indyuce.mmocore.quest.compat;

import me.blackvein.quests.Quest;
import me.blackvein.quests.Quester;
import me.blackvein.quests.Quests;
import net.Indyuce.mmocore.quest.AbstractQuest;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;


public class BlackVeinQuestsModule implements QuestModule<BlackVeinQuestsModule.BlackVeinQuestQuest> {
    private final Quests plugin = (Quests) Bukkit.getPluginManager().getPlugin("Quests");



    @Override
    public BlackVeinQuestQuest getQuestOrThrow(String id) {
        Quests plugin = (Quests) Bukkit.getPluginManager().getPlugin("Quests");
        return plugin.getQuestById(id)==null?null:new BlackVeinQuestQuest(plugin.getQuestById(id));
    }


    @Override
    public boolean hasCompletedQuest(String questId, Player player) {
        Quester quester = plugin.getQuester(player.getUniqueId());

        for(Quest quest:quester.getCompletedQuests()) {
            if(quest.getId().equals(questId))
                return true;
        }
        return false;
    }


    public class BlackVeinQuestQuest implements AbstractQuest {
        private final Quest quest;

        public BlackVeinQuestQuest(Quest quest) {
            this.quest = quest;
        }

        @Override
        public String getName() {
            return quest.getName();
        }

        @Override
        public String getId() {
            return quest.getId();
        }
    }

}
