package net.Indyuce.mmocore.manager.data;

import java.util.UUID;

import io.lumine.mythic.lib.data.OfflineDataHolder;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.profess.PlayerClass;

public interface OfflinePlayerData extends OfflineDataHolder {

    public abstract void removeFriend(UUID uuid);

    public abstract boolean hasFriend(UUID uuid);

    public abstract PlayerClass getProfess();

    public abstract int getLevel();

    public abstract long getLastLogin();

    public static OfflinePlayerData get(UUID uuid) {
        return MMOCore.plugin.dataProvider.getDataManager().getOffline(uuid);
    }
}
