package net.Indyuce.mmocore.gui.api.item;

import net.Indyuce.mmocore.MMOCore;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class Placeholders {
    private final Map<String, String> placeholders = new HashMap<>();

    public void register(String path, Object obj) {
        placeholders.put(path, obj.toString());
    }

    public String apply(Player player, String str) {

        // Remove conditions first
        str = removeCondition(str);

        // Internal placeholders
        while (str.contains("{") && str.substring(str.indexOf("{")).contains("}")) {
            String holder = str.substring(str.indexOf("{") + 1, str.indexOf("}"));
            @Nullable String found = placeholders.get(holder);

            /*
             * Do NOT replace the placeholder unless a corresponding value has
             * been found. This simple workaround fixes an issue with PAPI
             * math expansions which interferes with MMOCore placeholders since
             * it uses {....} as well.
             */
            if (found != null)
                str = str.replace("{" + holder + "}", found);
        }

        // External placeholders
        return MMOCore.plugin.placeholderParser.parse(player, str);
    }

    private String removeCondition(String str) {
        return str.startsWith("{") && str.contains("}") ? str.substring(str.indexOf("}") + 1) : str;
    }
}
