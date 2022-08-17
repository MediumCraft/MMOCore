package net.Indyuce.mmocore.api;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.manager.StatManager;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerActivity;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.gui.api.item.Placeholders;
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
                Placeholders holders=getActionBarPlaceholder(data);
                data.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(
                        holders.apply(data.getPlayer(), data.getProfess().hasActionBar() ? data.getProfess().getActionBar() : config.format)));


                /*
                data.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(MMOCore.plugin.placeholderParser.parse(data.getPlayer(),data.getProfess().hasActionBar() ? data.getProfess().getActionBar() : config.format));
                        MythicLib.plugin.parseColors((data.getProfess().hasActionBar() ? data.getProfess().getActionBar() : config.format)
                                .replace("{health}", digit.format(data.getPlayer().getHealth()))
                                .replace("{max_health}", StatManager.format("MAX_HEALTH", data.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()))
                                .replace("{mana_icon}", data.getProfess().getManaDisplay().getIcon())
                                .replace("{mana}", digit.format(data.getMana()))
                                .replace("{max_mana}", StatManager.format("MAX_MANA", data.getStats().getStat("MAX_MANA")))
                                .replace("{stamina}", digit.format(data.getStamina()))
                                .replace("{max_stamina}", StatManager.format("MAX_STAMINA", data.getStats().getStat("MAX_STAMINA")))
                                .replace("{stellium}", digit.format(data.getStellium()))
                                .replace("{max_stellium}", StatManager.format("MAX_STELLIUM", data.getStats().getStat("MAX_STELLIUM")))
                                .replace("{class}", data.getProfess().getName())
                                .replace("{xp}", MythicLib.plugin.getMMOConfig().decimal.format(data.getExperience()))
                                .replace("{armor}", StatManager.format("ARMOR", data.getPlayer().getAttribute(Attribute.GENERIC_ARMOR).getValue()))
                                .replace("{level}", "" + data.getLevel())
                                .replace("{name}", data.getPlayer().getDisplayName())))));*/
            }
    }

    public Placeholders getActionBarPlaceholder(PlayerData data) {
        Placeholders holders= new Placeholders();
        holders.register("health", digit.format(data.getPlayer().getHealth()));
                holders.register("max_health", StatManager.format("MAX_HEALTH", data.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
                holders.register("mana_icon", data.getProfess().getManaDisplay().getIcon());
                holders.register("mana", digit.format(data.getMana()));
                holders.register("max_mana", StatManager.format("MAX_MANA", data.getStats().getStat("MAX_MANA")));
                holders.register("stamina", digit.format(data.getStamina()));
                holders.register("max_stamina", StatManager.format("MAX_STAMINA", data.getStats().getStat("MAX_STAMINA")));
                holders.register("stellium", digit.format(data.getStellium()));
                holders.register("max_stellium", StatManager.format("MAX_STELLIUM", data.getStats().getStat("MAX_STELLIUM")));
                holders.register("class", data.getProfess().getName());
                holders.register("xp", MythicLib.plugin.getMMOConfig().decimal.format(data.getExperience()));
                holders.register("armor", StatManager.format("ARMOR", data.getPlayer().getAttribute(Attribute.GENERIC_ARMOR).getValue()));
                holders.register("level", "" + data.getLevel());
                holders.register("name", data.getPlayer().getDisplayName());
                return holders;
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
