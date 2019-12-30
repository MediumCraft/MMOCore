package net.Indyuce.mmocore.api.experience.source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.enchantment.EnchantItemEvent;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.experience.Profession;
import net.Indyuce.mmocore.api.experience.source.type.ExperienceSource;
import net.Indyuce.mmocore.api.load.MMOLineConfig;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.manager.profession.ExperienceManager;
import net.mmogroup.mmolib.MMOLib;

public class EnchantItemExperienceSource extends ExperienceSource<Void> {
	private final List<Enchantment> enchants = new ArrayList<>();

	public EnchantItemExperienceSource(Profession profession, MMOLineConfig config) {
		super(profession);

		if (config.contains("enchant"))
			for (String key : config.getString("enchant").split("\\,"))
				enchants.add(MMOLib.plugin.getVersion().getWrapper().getEnchantmentFromString(key.toLowerCase().replace("-", "_")));
	}

	@Override
	public boolean matches(PlayerData player, Void v) {
		return hasRightClass(player);
	}
 
	@Override
	public ExperienceManager<EnchantItemExperienceSource> newManager() {
		return new ExperienceManager<EnchantItemExperienceSource>() {

			@EventHandler(priority = EventPriority.HIGH)
			public void a(EnchantItemEvent event) {
				if (event.isCancelled())
					return;

				PlayerData player = PlayerData.get(event.getEnchanter());
				for (EnchantItemExperienceSource source : getSources())
					if (source.matches(player, null)) {
						Map<Enchantment, Integer> ench = new HashMap<>(event.getEnchantsToAdd());

						if (!source.enchants.isEmpty())
							for (Iterator<Enchantment> iterator = ench.keySet().iterator(); iterator.hasNext();)
								if (!source.enchants.contains(iterator.next()))
									iterator.remove();

						if (ench.isEmpty())
							continue;

						double exp = 0;
						for (Entry<Enchantment, Integer> entry : ench.entrySet())
							exp += MMOCore.plugin.enchantManager.getBaseExperience(entry.getKey()) * entry.getValue();
						giveExperience(player, (int) exp, event.getEnchantBlock().getLocation());
					}
			}
		};
	}
}
