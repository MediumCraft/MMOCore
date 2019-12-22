package net.Indyuce.mmocore.comp.mythicmobs;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ItemTier;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.NBTItem;

public class Lootsplosion implements Listener {
	private static final Random random = new Random();

	private final boolean colored;

	public Lootsplosion() {
		colored = Bukkit.getPluginManager().getPlugin("MMOItems") != null && MMOCore.plugin.getConfig().getBoolean("lootsplosion.mmoitems-color");
	}

	@EventHandler
	public void b(MythicMobDeathEvent event) {
		new LootsplosionHandler(event);
	}

	public class LootsplosionHandler implements Listener {

		private final List<ItemStack> drops;

		/*
		 * Y coordinate offset so the velocity is not directly negated when the
		 * item spawns on the ground
		 */
		private final double offset;

		public LootsplosionHandler(MythicMobDeathEvent event) {
			offset = event.getEntity().getHeight() / 2;
			drops = new ArrayList<>(event.getDrops());

			Bukkit.getPluginManager().registerEvents(this, MMOCore.plugin);
		}

		private void close() {
			ItemSpawnEvent.getHandlerList().unregister(this);
		}

		@EventHandler
		public void a(ItemSpawnEvent event) {
			Item item = event.getEntity();
			if (!drops.contains(item.getItemStack())) {
				close();
				return;
			}

			drops.remove(item.getItemStack());
			item.teleport(item.getLocation().add(0, offset, 0));
			item.setVelocity(randomVector());

			if (colored)
				Bukkit.getScheduler().runTaskAsynchronously(MMOCore.plugin, () -> {
					NBTItem nbt = MMOLib.plugin.getNMS().getNBTItem(item.getItemStack());
					if (nbt.hasTag("MMOITEMS_TIER")) {
						ItemTier tier = MMOItems.plugin.getTiers().get(nbt.getString("MMOITEMS_TIER"));
						if (tier.hasColor())
							new LootColor(item, tier.getColor().toBukkit());
					}
				});
		}

	}

	private Vector randomVector() {
		double offset = MMOCore.plugin.getConfig().getDouble("lootsplosion.offset"), height = MMOCore.plugin.getConfig().getDouble("lootsplosion.height");
		return new Vector(Math.cos(random.nextDouble() * Math.PI * 2) * offset, height, Math.sin(random.nextDouble() * Math.PI * 2) * offset);
	}
}
