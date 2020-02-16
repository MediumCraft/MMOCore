package net.Indyuce.mmocore.api.quest.trigger;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmocore.api.load.MMOLineConfig;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.item.SmartGive;

public class ItemTrigger extends Trigger {
	private final Material material;
	private final int amount;

	public ItemTrigger(MMOLineConfig config) {
		super(config);

		config.validate("type");

		material = Material.valueOf(config.getString("type").toUpperCase().replace("-", "_"));
		amount = config.contains("amount") ? Math.max(1, config.getInt("amount")) : 1;
	}

	@Override
	public void apply(PlayerData player) {
		new SmartGive(player.getPlayer()).give(new ItemStack(material, amount));
	}
}
