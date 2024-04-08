package net.Indyuce.mmocore.api;

import io.lumine.mythic.lib.MythicLib;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ConfigMessage {
    private final String key;
    private final List<String> lines = new ArrayList<>();
    private final boolean papiPlaceholders, actionbar, raw;

    private ConfigMessage(@NotNull String key) {
        this.key = key;

        // Initialize message list
        final Object obj = MMOCore.plugin.configManager.getMessageObject(key);
        if (obj == null) lines.add("<message_not_found:'" + key + "'>");
        else if (obj instanceof List<?>) lines.addAll((List<String>) obj);
        else {
            final String tostr = obj.toString();
            if (!tostr.isEmpty()) lines.add(tostr);
        }

        // Does message include placeholders
        boolean hasPlaceholders = false;
        for (String str : lines)
            if (str.contains("%")) {
                hasPlaceholders = true;
                break;
            }
        this.papiPlaceholders = hasPlaceholders;

        // Is message for action bar
        actionbar = !lines.isEmpty() && lines.get(0).startsWith("%");
        if (actionbar) lines.set(0, lines.get(0).substring(1));

        // Are messages raw (JSON format)
        raw = !lines.isEmpty() && (lines.get(0).startsWith("{") || lines.get(0).startsWith("["));
    }

    /**
     * Useful for things like indicators or specific lore lines
     * which are string tags not requiring more than one string
     * object. An empty return value is accepted as some features
     * do require the ability to fully remove text.
     *
     * @return First line of message, if it exists.
     */
    @NotNull
    public String asLine() {
        return lines.isEmpty() ? "" : lines.get(0);
    }

    @NotNull
    public List<String> getLines() {
        return lines;
    }

    @NotNull
    public ConfigMessage addPlaceholders(@NotNull Object... placeholders) {

        for (int n = 0; n < lines.size(); n++) {
            String line = lines.get(n);

            // Apply placeholders to string
            for (int j = 0; j < placeholders.length - 1; j += 2) {
                final String placeholder = String.valueOf(placeholders[j]);
                line = line.replace("{" + placeholder + "}", String.valueOf(placeholders[j + 1]));
            }

            lines.set(n, line);
        }

        return this;
    }

    @Deprecated
    public void sendAsJSon(Player player) {
        send(player);
    }

    public void send(Player player) {
        for (String line : lines) send(player, line);
    }

    public void send(Collection<? extends Player> players) {
        for (Player player : players) for (String line : lines) send(player, line);
    }

    /**
     * Sends a line of text to a target player
     *
     * @param player        Player to send message to. His player
     *                      data is not necessarily fully loaded
     * @param messageFormat Raw/normal message to send
     */
    private void send(@NotNull Player player, String messageFormat) {
        Validate.notNull(player, "Player cannot be null");

        final String rawMessage = format(player, messageFormat);
        final PlayerData playerData = PlayerData.has(player) ? PlayerData.get(player) : null;

        // Handle special case with player data + action bar
        if (playerData != null && playerData.isOnline() && actionbar) {
            playerData.displayActionBar(rawMessage, raw);
            return;
        }

        // Normal sender
        if (this.raw) {
            if (actionbar) MythicLib.plugin.getVersion().getWrapper().sendActionBarRaw(player, rawMessage);
            else MythicLib.plugin.getVersion().getWrapper().sendJson(player, rawMessage);
        } else {
            if (actionbar)
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(rawMessage));
            else player.sendMessage(rawMessage);
        }
    }

    @NotNull
    private String format(@Nullable Player player, String input) {
        input = MythicLib.plugin.parseColors(input);
        if (!papiPlaceholders || player == null) return input; // Optimization
        return MMOCore.plugin.placeholderParser.parse(player, input);
    }

    @NotNull
    public static ConfigMessage fromKey(@NotNull String key, Object... placeholders) {
        Validate.notNull(MMOCore.plugin.configManager, "MMOCore has not finished enabling");
        final ConfigMessage message = new ConfigMessage(key);
        if (placeholders.length != 0) message.addPlaceholders(placeholders);
        return message;
    }
}
