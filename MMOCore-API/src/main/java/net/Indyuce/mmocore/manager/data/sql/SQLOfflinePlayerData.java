package net.Indyuce.mmocore.manager.data.sql;

import net.Indyuce.mmocore.api.player.profess.PlayerClass;
import net.Indyuce.mmocore.manager.data.OfflinePlayerData;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * @deprecated Not implemented yet
 */
@Deprecated
public class SQLOfflinePlayerData implements OfflinePlayerData {
    private final UUID uuid;
    private int level;
    private long lastLogin;
    private PlayerClass profess;
    private List<UUID> friends;

    @Deprecated
    public SQLOfflinePlayerData(UUID uuid) {
        this.uuid = uuid;
/*
            provider.getResult("SELECT * FROM mmocore_playerdata WHERE uuid = '" + uuid + "';", (result) -> {
                try {
                    MythicLib.debug("MMOCoreSQL", "Loading OFFLINE data for '" + uuid + "'.");
                    if (!result.next()) {
                        level = 0;
                        lastLogin = 0;
                        profess = MMOCore.plugin.classManager.getDefaultClass();
                        friends = new ArrayList<>();
                        MythicLib.debug("MMOCoreSQL", "Default OFFLINE data loaded.");
                    } else {
                        level = result.getInt("level");
                        lastLogin = result.getLong("last_login");
                        profess = isEmpty(result.getString("class")) ? MMOCore.plugin.classManager.getDefaultClass() : MMOCore.plugin.classManager.get(result.getString("class"));
                        if (!isEmpty(result.getString("friends")))
                            MMOCoreUtils.jsonArrayToList(result.getString("friends")).forEach(str -> friends.add(UUID.fromString(str)));
                        else friends = new ArrayList<>();
                        MythicLib.debug("MMOCoreSQL", "Saved OFFLINE data loaded.");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }); */
    }

    @Override
    @NotNull
    public UUID getUniqueId() {
        return uuid;
    }

    @Override
    public void removeFriend(UUID uuid) {
        // TODO recode
        //  friends.remove(uuid);
        //  new PlayerDataTableUpdater(provider, uuid).updateData("friends", friends.stream().map(UUID::toString).collect(Collectors.toList()));
    }

    @Override
    public boolean hasFriend(UUID uuid) {
        return friends.contains(uuid);
    }

    @Override
    public PlayerClass getProfess() {
        return profess;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public long getLastLogin() {
        return lastLogin;
    }
}
