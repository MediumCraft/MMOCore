package net.Indyuce.mmocore.manager;

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

public class ActionBarManager extends BukkitRunnable {
    private int updateTicks, timeOut;
    private String barFormat;
    private boolean enabled, scheduled;

    public void reload(ConfigurationSection config) {
        enabled = config.getBoolean("enabled", false);
        timeOut = config.getInt("", 60);
        updateTicks = config.getInt("ticks-to-update", 5);
        barFormat = config.getString("format", "<No Action Bar Format Found>");

        if (!scheduled && enabled) {
            runTaskTimer(MMOCore.plugin, 0, updateTicks);
            scheduled = true;
        } else if (scheduled && !enabled) {
            cancel();
            scheduled = false;
        }
    }

    public long getTimeOut() {
        return timeOut;
    }

    @Override
    public void run() {
        for (PlayerData data : PlayerData.getAll())
            if (data.isOnline() && !data.getPlayer().isDead() && !data.isCasting() && data.getActivityTimeOut(PlayerActivity.ACTION_BAR_MESSAGE) == 0) {
                Placeholders holders = getActionBarPlaceholders(data);
                data.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(
                        holders.apply(data.getPlayer(), data.getProfess().hasActionBar() ? data.getProfess().getActionBar() : barFormat)));

            }
    }

    public Placeholders getActionBarPlaceholders(PlayerData data) {
        Placeholders holders = new Placeholders();
        holders.register("health", StatManager.format("MAX_HEALTH", data.getPlayer().getHealth()));
        holders.register("max_health", StatManager.format("MAX_HEALTH", data.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
        holders.register("mana_icon", data.getProfess().getManaDisplay().getIcon());
        holders.register("mana", StatManager.format("MAX_MANA", data.getMana()));
        holders.register("max_mana", StatManager.format("MAX_MANA", data.getStats().getStat("MAX_MANA")));
        holders.register("stamina", StatManager.format("MAX_STAMINA", data.getStamina()));
        holders.register("max_stamina", StatManager.format("MAX_STAMINA", data.getStats().getStat("MAX_STAMINA")));
        holders.register("stellium", StatManager.format("MAX_STELLIUM", data.getStellium()));
        holders.register("max_stellium", StatManager.format("MAX_STELLIUM", data.getStats().getStat("MAX_STELLIUM")));
        holders.register("class", data.getProfess().getName());
        holders.register("xp", MythicLib.plugin.getMMOConfig().decimal.format(data.getExperience()));
        holders.register("armor", StatManager.format("ARMOR", data.getPlayer().getAttribute(Attribute.GENERIC_ARMOR).getValue()));
        holders.register("level", String.valueOf(data.getLevel()));
        holders.register("name", data.getPlayer().getDisplayName());
        return holders;
    }
}
