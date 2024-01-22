package net.Indyuce.mmocore.api.quest.trigger;

import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.api.util.SmartGive;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;


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
		if(!player.isOnline()) return;
		new SmartGive(player.getPlayer()).give(new ItemStack(material, amount));
	}
}
