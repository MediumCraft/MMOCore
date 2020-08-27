package net.Indyuce.mmocore.api.experience.source;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmocore.api.experience.Profession;
import net.Indyuce.mmocore.api.experience.source.type.SpecificExperienceSource;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.manager.profession.ExperienceManager;
import net.mmogroup.mmolib.api.MMOLineConfig;

public class FishItemExperienceSource extends SpecificExperienceSource<ItemStack> {
	private final Material material;

	public FishItemExperienceSource(Profession profession, MMOLineConfig config) {
		super(profession, config);

		config.validate("type");
		material = Material.valueOf(config.getString("type").toUpperCase().replace("-", "_").replace(" ", "_"));
	}

	@Override
	public ExperienceManager<FishItemExperienceSource> newManager() {
		return new ExperienceManager<FishItemExperienceSource>() {

			@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
			public void a(PlayerFishEvent event) {
				if (event.getState() == State.CAUGHT_FISH) {
					ItemStack caught = ((Item) event.getCaught()).getItemStack();
					if (caught.hasItemMeta())
						return;

					PlayerData data = PlayerData.get(event.getPlayer());
					for (FishItemExperienceSource source : getSources())
						if (source.matches(data, caught))
							source.giveExperience(data, caught.getAmount(), event.getHook().getLocation().add(0, 1.0f, 0));
				}
			}
		};
	}

	@Override
	public boolean matches(PlayerData player, ItemStack obj) {
		return hasRightClass(player) && obj.getType() == material;
	}
}
