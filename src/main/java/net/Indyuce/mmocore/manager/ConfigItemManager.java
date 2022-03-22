package net.Indyuce.mmocore.manager;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigFile;
import net.Indyuce.mmocore.util.item.ConfigItem;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class ConfigItemManager implements MMOCoreManager {
    private final Map<String, ConfigItem> map = new HashMap<>();

    public void register(ConfigItem item) {
        map.put(item.getId(), item);
    }

    @Nullable
    public ConfigItem get(String id) {
        return map.get(id);
    }

    @Override
    public void initialize(boolean clearBefore) {
        if (clearBefore)
            map.clear();

        FileConfiguration config = new ConfigFile("items").getConfig();
        for (String key : config.getKeys(false))
            try {
                register(new ConfigItem(config.getConfigurationSection(key)));
            } catch (NullPointerException | IllegalArgumentException exception) {
                MMOCore.plugin.getLogger().log(Level.WARNING, "Could not load config item " + key);
            }
    }
}
