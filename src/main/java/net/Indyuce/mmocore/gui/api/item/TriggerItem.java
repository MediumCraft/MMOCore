package net.Indyuce.mmocore.gui.api.item;

import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.quest.trigger.Trigger;
import net.Indyuce.mmocore.gui.api.GeneratedInventory;
import org.bukkit.configuration.ConfigurationSection;

public class TriggerItem extends InventoryItem {
    private final Trigger trigger;

    public TriggerItem(ConfigurationSection config, String format) {
        super(config);

        trigger = MMOCore.plugin.loadManager.loadTrigger(new MMOLineConfig(format));
    }

    @Override
    public Placeholders getPlaceholders(GeneratedInventory inv, int n) {
        return new Placeholders();
    }

    public Trigger getTrigger() {
        return trigger;
    }
}
