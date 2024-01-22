package net.Indyuce.mmocore.manager.data.yaml;

import java.util.List;
import java.util.UUID;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.profess.PlayerClass;
import net.Indyuce.mmocore.api.ConfigFile;
import net.Indyuce.mmocore.manager.data.OfflinePlayerData;
import org.jetbrains.annotations.NotNull;

/**
 * @deprecated Not implemented yet
 */
@Deprecated
public class YAMLOfflinePlayerData implements OfflinePlayerData {
    private final UUID uuid;
    private final ConfigFile config;

    /**
     * Supports offline player data operations like friend removals which can't
     * be handled when their player data is not loaded in the data map.
     */
    @Deprecated
    public YAMLOfflinePlayerData(UUID uuid) {
        this.uuid = uuid;
        config = new ConfigFile(uuid);
    }

    @Override
    @NotNull
    public UUID getUniqueId() {
        return uuid;
    }

    @Override
    public void removeFriend(UUID uuid) {
        List<String> friends = config.getConfig().getStringList("friends");
        friends.remove(uuid.toString());
        config.getConfig().set("friends", friends);
        config.save();
    }

    @Override
    public boolean hasFriend(UUID uuid) {
        return config.getConfig().getStringList("friends").contains(uuid.toString());
    }

    @Override
    public PlayerClass getProfess() {
        return config.getConfig().contains("class") ? MMOCore.plugin.classManager.get(config.getConfig().getString("class")) : MMOCore.plugin.classManager.getDefaultClass();
    }

    @Override
    public int getLevel() {
        return config.getConfig().getInt("level");
    }

    @Override
    public long getLastLogin() {
        return config.getConfig().getLong("last-login");
    }
}
