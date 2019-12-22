package net.Indyuce.mmocore.listener.profession;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BrewingStand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.scheduler.BukkitRunnable;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.mmogroup.mmolib.api.NBTItem;

public class Alchemy implements Listener {
	private Set<String> runnables = new HashSet<>();

	private static Set<BrewingRecipe> recipes = new HashSet<>();

	public Alchemy(ConfigurationSection config) {
		Alchemy.recipes.clear();
		for (String key : config.getKeys(false)) {
			BrewingRecipe recipe = new BrewingRecipe(config.getConfigurationSection(key));
			if (recipe.isValid())
				recipes.add(recipe);
		}
	}

	/*
	 * force place the item in the brewing stand inventory so it can start the
	 * recipe.
	 */
	@EventHandler
	public void a(InventoryClickEvent event) {
		if (event.getInventory().getType() != InventoryType.BREWING)
			return;

		event.setCancelled(true);
		ItemStack item = event.getCurrentItem();
		if (item == null || item.getType() == Material.AIR)
			return;

		Player player = (Player) event.getWhoClicked();
		BrewingStand stand = (BrewingStand) event.getInventory().getHolder();
		BrewerInventory inv = (BrewerInventory) event.getInventory();

		// send ingredient back in the player inventory
		if (event.getRawSlot() == 3) {
			if (player.getInventory().firstEmpty() == -1)
				return;

			player.getInventory().addItem(inv.getIngredient());
			inv.setIngredient(null);

			player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 1);
			checkForBrewing(player, stand);
			return;
		}

		// send fuel back in the player inventory
		if (event.getRawSlot() == 4) {
			if (player.getInventory().firstEmpty() == -1)
				return;

			player.getInventory().addItem(inv.getFuel());
			inv.setFuel(null);

			player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 1);
			checkForBrewing(player, stand);
			return;
		}

		// send bottle back in the player inventory
		if (event.getRawSlot() >= 0 && event.getRawSlot() < 3) {
			if (player.getInventory().firstEmpty() == -1)
				return;

			player.getInventory().addItem(inv.getItem(event.getRawSlot()));
			inv.setItem(event.getRawSlot(), null);

			player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 1);
			checkForBrewing(player, stand);
			return;
		}

		BrewingRecipe recipe = getCorrespondingRecipe(NBTItem.get(item));

		// send fuel in the brewing stand
		if (recipe == null) {
			if (item.getType() == Material.BLAZE_POWDER && (inv.getFuel() == null || inv.getFuel().getType() == Material.AIR || item.isSimilar(inv.getFuel()))) {
				int fuel = inv.getFuel() == null ? 0 : inv.getFuel().getAmount();
				int needed = 64 - fuel;
				int sent = Math.min(needed, item.getAmount());

				if (sent == 0)
					return;

				ItemStack fuelItem = item.clone();
				fuelItem.setAmount(fuel + sent);
				inv.setFuel(fuelItem);

				if (sent == item.getAmount())
					event.setCurrentItem(null);
				else
					item.setAmount(item.getAmount() - sent);

				player.playSound(player.getLocation(), Sound.BLOCK_END_PORTAL_FRAME_FILL, 1, 2);
				checkForBrewing(player, stand);
				return;
			}

			// send potion in the brewing stand
			if (item.getType() == Material.POTION && ((PotionMeta) item.getItemMeta()).getCustomEffects().isEmpty()) {
				int empty = getEmptyBottleSlot(inv);
				if (empty == -1)
					return;

				inv.setItem(empty, event.getCurrentItem());
				event.setCurrentItem(null);

				player.playSound(player.getLocation(), Sound.BLOCK_END_PORTAL_FRAME_FILL, 1, 2);
				checkForBrewing(player, stand);
			}
			return;
		}

		// send ingredient in the brewing stand
		if (inv.getIngredient() == null || inv.getIngredient().getType() == Material.AIR || item.isSimilar(inv.getIngredient())) {
			int ingredient = inv.getIngredient() == null ? 0 : inv.getIngredient().getAmount();
			int needed = 64 - ingredient;
			int sent = Math.min(needed, item.getAmount());

			if (sent == 0)
				return;

			ItemStack ingredientItem = item.clone();
			ingredientItem.setAmount(ingredient + sent);
			inv.setIngredient(ingredientItem);

			if (sent == item.getAmount())
				event.setCurrentItem(null);
			else
				item.setAmount(item.getAmount() - sent);

			player.playSound(player.getLocation(), Sound.BLOCK_END_PORTAL_FRAME_FILL, 1, 2);
			checkForBrewing(player, stand);
		}
	}

	private void checkForBrewing(Player player, BrewingStand stand) {
		BrewingRecipe recipe = getCorrespondingRecipe(NBTItem.get(stand.getInventory().getIngredient()));
		Location loc = stand.getLocation();
		if (!runnables.contains(loc.getBlockY() + "-" + loc.getBlockY() + "-" + loc.getBlockZ()) && recipe != null && stand.getFuelLevel() > 0 && hasAtLeastOnePotion(stand.getInventory()))
			new BrewingRunnable(player, stand).start(recipe);
	}

	private int getEmptyBottleSlot(BrewerInventory inv) {
		ItemStack item;
		for (int j = 0; j < 3; j++)
			if ((item = inv.getItem(j)) == null || item.getType() == Material.AIR)
				return j;
		return -1;
	}

	/*
	 * returns if the corresponding brewing inventory has at least ONE potion
	 * and if
	 */
	private boolean hasAtLeastOnePotion(BrewerInventory inv) {
		ItemStack item;
		for (int j = 0; j < 3; j++)
			if ((item = inv.getItem(j)) != null)
				return item.getType() == Material.POTION;
		return false;
	}

	private boolean hasNoBottle(BrewerInventory inv) {
		ItemStack item;
		for (int j = 0; j < 3; j++)
			if ((item = inv.getItem(j)) != null && item.getType() == Material.POTION)
				return false;
		return true;
	}

	private BrewingRecipe getCorrespondingRecipe(NBTItem item) {
		for (BrewingRecipe recipe : recipes)
			if (recipe.matchesIngredient(item))
				return recipe;
		return null;
	}

	private ItemStack consume(ItemStack item) {
		if (item.getAmount() < 2)
			return null;

		item.setAmount(item.getAmount() - 1);
		return item;
	}

	public class BrewingRunnable extends BukkitRunnable {
		private int time = 0;
		private Block block;
		private Location loc;
		private BrewingRecipe recipe;
		private String mapPath;
		private Player player;

		public BrewingRunnable(Player player, BrewingStand stand) {
			this.block = stand.getBlock();
			this.player = player;
			loc = stand.getLocation().add(.5, .5, .5);
		}

		public void start(BrewingRecipe recipe) {
			this.recipe = recipe;
			BrewingStand stand = (BrewingStand) block.getState();
			stand.setFuelLevel(stand.getFuelLevel() - 1);
			stand.update();
			runnables.add(mapPath = loc.getBlockY() + "-" + loc.getBlockY() + "-" + loc.getBlockZ());
			runTaskTimer(MMOCore.plugin, 1, 1);
		}

		@Override
		public void run() {
			BrewingStand stand = (BrewingStand) block.getState();
			stand.getWorld().spawnParticle(Particle.SPELL_MOB, loc.clone().add(Math.cos((double) time / 3.) * .4, 0, Math.sin((double) time / 3.) * .4), 0);

			// cancel the recipe if ingredient was changed
			if (!recipe.matchesIngredient(stand.getInventory().getIngredient()) || hasNoBottle(stand.getInventory())) {
				runnables.remove(mapPath);
				cancel();
				return;
			}

			if (time++ > recipe.getCookingTime()) {
				runnables.remove(mapPath);
				stand.getInventory().setIngredient(consume(stand.getInventory().getIngredient()));

				int count = 0;
				ItemStack item, result = recipe.getResult();
				for (int j = 0; j < 3; j++)
					if ((item = stand.getInventory().getItem(j)) != null && item.getType() != Material.AIR) {
						count++;
						stand.getInventory().setItem(j, result);
					}

				if (MMOCore.plugin.professionManager.has("alchemy"))
					PlayerData.get(player).getCollectionSkills().giveExperience(MMOCore.plugin.professionManager.get("alchemy"), count * recipe.experience, loc);

				stand.getWorld().playSound(stand.getLocation(), Sound.BLOCK_BREWING_STAND_BREW, 1, 1);
				cancel();
				return;
			}

			stand.setBrewingTime((int) (400. * (1. - (double) time / recipe.getCookingTime())));
			stand.update();
		}
	}

	public class BrewingRecipe {

		// ingredient
		private Type ingredientType;
		private String ingredientId;

		private int time, experience;

		// result
		private Type type;
		private String id;

		private boolean valid = true;

		public BrewingRecipe(ConfigurationSection section) {
			try {
				String[] split = section.getString("ingredient").split("\\.");
				ingredientType = MMOItems.plugin.getTypes().get(split[0]);
				ingredientId = split[1];

				split = section.getString("result").split("\\.");
				type = MMOItems.plugin.getTypes().get(split[0]);
				id = split[1];

				time = (int) (section.getDouble("cook-time") * 20.);
				experience = section.getInt("exp");
			} catch (Exception e) {
				MMOCore.plugin.getLogger().log(Level.WARNING, "Could not register brewing recipe named " + section.getName());
				valid = false;
			}
		}

		public BrewingRecipe(Type ingredientType, String ingredientId, int time, Type type, String id) {
			this.ingredientType = ingredientType;
			this.ingredientId = ingredientId;
			this.time = time;
			this.type = type;
			this.id = id;
		}

		public boolean isValid() {
			return valid;
		}

		public boolean matchesIngredient(NBTItem nbt) {
			return nbt.getString("MMOITEMS_ITEM_TYPE").equals(ingredientType.getId()) && nbt.getString("MMOITEMS_ITEM_ID").equals(ingredientId);
		}

		public boolean matchesIngredient(ItemStack item) {
			return matchesIngredient(NBTItem.get(item));
		}

		public int getCookingTime() {
			return time;
		}

		public ItemStack getResult() {
			return MMOItems.plugin.getItems().getItem(type, id);
		}
	}

	// @EventHandler
	// public void a(ProjectileLaunchEvent event) {
	// if (!(event.getEntity() instanceof ThrownPotion) ||
	// !(event.getEntity().getShooter() instanceof Player))
	// return;
	//
	// double c = 1 + random.nextDouble() * 2;
	//
	// Vector vec = event.getEntity().getVelocity();
	// vec.setX(vec.getX() * c);
	// vec.setZ(vec.getZ() * c);
	// event.getEntity().setVelocity(vec);
	//
	// new PotionParticles((ThrownPotion) event.getEntity()).start();
	// }
}
