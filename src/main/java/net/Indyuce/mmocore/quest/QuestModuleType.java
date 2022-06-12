package net.Indyuce.mmocore.quest;

import net.Indyuce.mmocore.quest.compat.BeautyQuestsModule;
import net.Indyuce.mmocore.quest.compat.BlackVeinQuestsModule;
import net.Indyuce.mmocore.quest.compat.QuestCreatorModule;
import net.Indyuce.mmocore.quest.compat.QuestModule;
import org.bukkit.Bukkit;

import javax.inject.Provider;

public enum QuestModuleType {
    MMOCORE("MMOCore", MMOCoreQuestModule::new),
    QUESTS("Quests", BlackVeinQuestsModule::new),
    BEAUTY_QUEST("BeautyQuests", BeautyQuestsModule::new),
    QUEST_CREATOR("QuestCreator", QuestCreatorModule::new);

    private final String pluginName;
    private final Provider<QuestModule> provider;

    QuestModuleType(String pluginName, Provider<QuestModule> provider) {
        this.pluginName = pluginName;
        this.provider = provider;
    }

    public boolean isValid() {
        return Bukkit.getPluginManager().getPlugin(pluginName) != null;
    }

    public QuestModule provideModule() {
        return provider.get();
    }
}
