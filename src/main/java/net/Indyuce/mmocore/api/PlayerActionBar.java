package net.Indyuce.mmocore.api;

import io.lumine.mythic.lib.MythicLib;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerActivity;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.player.stats.StatInfo;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.DecimalFormat;

public class PlayerActionBar extends BukkitRunnable {
    boolean initialized = false;

    private ActionBarConfig config;
    private DecimalFormat digit;

    public void reload(ConfigurationSection cfg) {
        config = new ActionBarConfig(cfg);
        digit = MythicLib.plugin.getMMOConfig().newDecimalFormat(config.digit);

        if (!initialized && config.enabled) {
            runTaskTimer(MMOCore.plugin, 0, config.ticks);
            initialized = true;
        }
    }

    public long getTimeOut() {
        return config.timeout;
    }

    @Override
    public void run() {
        for (PlayerData data : PlayerData.getAll())
            if (data.isOnline() && !data.getPlayer().isDead() && !data.isCasting() && data.getActivityTimeOut(PlayerActivity.ACTION_BAR_MESSAGE) == 0) {
                data.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(MMOCore.plugin.placeholderParser.parse(data.getPlayer(),
                        MythicLib.plugin.parseColors((data.getProfess().hasActionBar() ? data.getProfess().getActionBar() : config.format)
                                .replace("{health}", digit.format(data.getPlayer().getHealth()))
                                .replace("{max_health}", StatInfo.valueOf("MAX_HEALTH").format(data.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()))
                                .replace("{mana_icon}", data.getProfess().getManaDisplay().getIcon())
                                .replace("{mana}", digit.format(data.getMana()))
                                .replace("{max_mana}", StatInfo.valueOf("MAX_MANA").format(data.getStats().getStat("MAX_MANA")))
                                .replace("{stamina}", digit.format(data.getStamina()))
                                .replace("{max_stamina}", StatInfo.valueOf("MAX_STAMINA").format(data.getStats().getStat("MAX_STAMINA")))
                                .replace("{stellium}", digit.format(data.getStellium()))
                                .replace("{max_stellium}", StatInfo.valueOf("MAX_STELLIUM").format(data.getStats().getStat("MAX_STELLIUM")))
                                .replace("{class}", data.getProfess().getName())
                                .replace("{xp}", MythicLib.plugin.getMMOConfig().decimal.format(data.getExperience()))
                                .replace("{armor}", StatInfo.valueOf("ARMOR").format(data.getPlayer().getAttribute(Attribute.GENERIC_ARMOR).getValue()))
                                .replace("{level}", "" + data.getLevel())
                                .replace("{name}", data.getPlayer().getDisplayName())))));
            }
    }

    private static class ActionBarConfig {
        private final boolean enabled;
        private final int ticks, timeout;
        private final String digit, format;

        private ActionBarConfig(ConfigurationSection config) {
            enabled = config.getBoolean("enabled", false);
            timeout = config.getInt("", 60);
            digit = config.getString("decimal", "0.#");
            ticks = config.getInt("ticks-to-update", 5);
            format = config.getString("format", "please format me :c");
        }
    }
}
