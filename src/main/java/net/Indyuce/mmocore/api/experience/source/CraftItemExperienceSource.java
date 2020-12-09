package net.Indyuce.mmocore.api.experience.source;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.CraftItemEvent;

import net.Indyuce.mmocore.api.experience.Profession;
import net.Indyuce.mmocore.api.experience.source.type.SpecificExperienceSource;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.manager.profession.ExperienceManager;
import net.mmogroup.mmolib.api.MMOLineConfig;
import org.bukkit.event.inventory.InventoryAction;

public class CraftItemExperienceSource extends SpecificExperienceSource<Material> {
	public final Material material;

	public CraftItemExperienceSource(Profession profession, MMOLineConfig config) {
		super(profession, config);

		config.validate("type");
		material = Material.valueOf(config.getString("type").toUpperCase().replace("-", "_").replace(" ", "_"));
	}

	@Override
	public ExperienceManager<CraftItemExperienceSource> newManager() {
		return new ExperienceManager<CraftItemExperienceSource>() {
			@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
			public void a(CraftItemEvent event) {
				if(event.getAction() == InventoryAction.NOTHING) return;
				PlayerData data = PlayerData.get((Player) event.getWhoClicked());
				for (CraftItemExperienceSource source : getSources())
					if (source.matches(data, event.getInventory().getResult().getType()))
						source.giveExperience(data, event.getInventory().getResult().getAmount(), event.getInventory().getLocation());
			}
		};
	}

	@Override
	public boolean matches(PlayerData player, Material obj) {
		return material == obj && hasRightClass(player);
	}
}
