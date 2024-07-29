package net.Indyuce.mmocore.manager;

import io.lumine.mythic.lib.util.FileUtils;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.quest.Quest;
import org.apache.commons.lang.Validate;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

public class QuestManager implements MMOCoreManager {
    private final Map<String, Quest> quests = new LinkedHashMap<>();

    public void register(Quest quest) {
        quests.put(quest.getId(), quest);
    }

    public Quest get(String id) {
        return quests.get(id);
    }

    public Quest getOrThrow(String id) {
        Validate.isTrue(quests.containsKey(id), "Could not find quest with ID '" + id + "'");
        return get(id);
    }

    public Collection<Quest> getAll() {
        return quests.values();
    }

    @Override
    public void initialize(boolean clearBefore) {
        if (clearBefore) quests.clear();

        FileUtils.loadObjectsFromFolder(MMOCore.plugin, "quests", true, (key, config) -> {
            register(new Quest(key, config));
        }, "Could not load quest from file '%s': %s");

        for (Quest quest : quests.values())
            try {
                quest.getPostLoadAction().performAction();
            } catch (IllegalArgumentException exception) {
                MMOCore.plugin.getLogger().log(Level.WARNING, "Could not post-load quest '" + quest.getId() + "': " + exception.getMessage());
            }
    }
}
