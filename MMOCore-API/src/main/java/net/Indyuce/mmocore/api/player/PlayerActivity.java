package net.Indyuce.mmocore.api.player;

import net.Indyuce.mmocore.MMOCore;

import javax.inject.Provider;
import java.util.Objects;

/**
 * Used by MMOCore when it has to store the last time a player
 * did some action. This also features a time out function which
 * can be used for cooldowns.
 *
 * @deprecated Merge with {@link io.lumine.mythic.lib.player.cooldown.CooldownMap}
 */
public enum PlayerActivity {
    USE_WAYPOINT(() -> 5 * 1000L),

    FRIEND_REQUEST(() -> 1000 * 60 * 2L),

    ACTION_BAR_MESSAGE(() -> MMOCore.plugin.actionBarManager.getTimeOut() * 50),

    LOOT_CHEST_SPAWN(() -> MMOCore.plugin.configManager.lootChestPlayerCooldown),

    CAST_SKILL(() -> MMOCore.plugin.configManager.globalSkillCooldown),

    ;

    private final Provider<Long> timeout;

    PlayerActivity(Provider<Long> timeout) {
        this.timeout = timeout;
    }

    public long getTimeOut() {
        return Objects.requireNonNull(timeout, "Time out not supported").get();
    }
}
