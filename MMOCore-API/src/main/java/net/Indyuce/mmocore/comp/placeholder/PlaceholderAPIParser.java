package net.Indyuce.mmocore.comp.placeholder;

import io.lumine.mythic.lib.MythicLib;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.OfflinePlayer;

public class PlaceholderAPIParser implements PlaceholderParser {
    public PlaceholderAPIParser() {
        new RPGPlaceholders().register();
    }

    @Override
    public String parse(OfflinePlayer player, String string) {
        return MythicLib.plugin.parseColors(PlaceholderAPI.setPlaceholders(player, string));
    }
}
