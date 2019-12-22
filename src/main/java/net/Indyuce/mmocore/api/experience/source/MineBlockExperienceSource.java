package net.Indyuce.mmocore.api.experience.source;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmocore.api.experience.Profession;
import net.Indyuce.mmocore.api.experience.source.type.SpecificExperienceSource;
import net.Indyuce.mmocore.api.load.MMOLineConfig;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.manager.profession.ExperienceManager;
import net.mmogroup.mmolib.MMOLib;

public class MineBlockExperienceSource extends SpecificExperienceSource<Material> {
	public final Material material;
	private final boolean silkTouch;
	private final boolean crop;
	private final boolean playerPlaced;

	public MineBlockExperienceSource(Profession profession, MMOLineConfig config) {
		super(profession, config);

		config.validate("type");
		material = Material.valueOf(config.getString("type").toUpperCase().replace("-", "_").replace(" ", "_"));
		silkTouch = config.getBoolean("silk-touch", true);
		crop = config.getBoolean("crop", false);
		playerPlaced = config.getBoolean("player-placed", false);
	}

	@Override
	public ExperienceManager<MineBlockExperienceSource> newManager() {
		return new ExperienceManager<MineBlockExperienceSource>() {

			@EventHandler(priority = EventPriority.HIGHEST)
			public void a(BlockBreakEvent event) {
				if (event.isCancelled() || event.getPlayer().getGameMode() != GameMode.SURVIVAL)
					return;
				PlayerData data = PlayerData.get(event.getPlayer());
				
				for (MineBlockExperienceSource source : getSources())
				{
					if (source.silkTouch && hasSilkTouch(event.getPlayer().getInventory().getItemInMainHand()))
						continue;
					if (source.crop && !MMOLib.plugin.getVersion().getWrapper().isCropFullyGrown(event.getBlock()))
						continue;
					if ((!source.playerPlaced) && event.getBlock().hasMetadata("player_placed"))
						continue;

					if (source.matches(data, event.getBlock().getType()))
						source.giveExperience(data, event.getBlock().getLocation());
				}
			}
		};
	}

	private boolean hasSilkTouch(ItemStack item) {
		return item != null && item.hasItemMeta() && item.getItemMeta().hasEnchant(Enchantment.SILK_TOUCH);
	}

	@Override
	public boolean matches(PlayerData player, Material obj) {
		return material == obj && hasRightClass(player);
	}
}
