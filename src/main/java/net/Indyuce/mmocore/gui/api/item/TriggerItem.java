package net.Indyuce.mmocore.gui.api.item;

import org.bukkit.configuration.ConfigurationSection;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.quest.trigger.Trigger;
import net.Indyuce.mmocore.gui.api.PluginInventory;
import net.mmogroup.mmolib.api.MMOLineConfig;

public class TriggerItem extends InventoryPlaceholderItem {
	private final Trigger trigger;
	
	public TriggerItem(ConfigurationSection config, String format) {
		super(config);
		
		trigger = MMOCore.plugin.loadManager.loadTrigger(new MMOLineConfig(format));
	}

	@Override
	public Placeholders getPlaceholders(PluginInventory inv, int n) {
		return new Placeholders();
	}
	
	public Trigger getTrigger() {
		return trigger;
	}
}
