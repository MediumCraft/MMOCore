package net.Indyuce.mmocore.listener.profession;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.item.NBTItem;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;

public class Smelting implements Listener {

	/*
	 * FurnaceSmeltEvent is called when an item has actually been smelted.
	 * FurnaceBurnEvent is called when a fuel piece is used to refill up the
	 * furnace fuel bar.
	 */

	private static Set<SmeltingRecipe> recipes = new HashSet<>();
	private static Set<NamespacedKey> vanillaKeys = new HashSet<>();

	public Smelting(ConfigurationSection config) {
		for (Iterator<Recipe> iterator = Bukkit.recipeIterator(); iterator.hasNext();) {
			Recipe recipe = iterator.next();
			if (recipe instanceof FurnaceRecipe && vanillaKeys.contains(((FurnaceRecipe) recipe).getKey()))
				iterator.remove();
		}

		Smelting.recipes.clear();
		Smelting.vanillaKeys.clear();

		for (String key : config.getKeys(false)) {
			SmeltingRecipe recipe = new SmeltingRecipe(config.getConfigurationSection(key));
			if (recipe.isValid()) {
				recipes.add(recipe);
				NamespacedKey vanillaKey = new NamespacedKey(MMOCore.plugin, "furnace_recipe_" + key.replace("-", "_").toLowerCase());
				vanillaKeys.add(vanillaKey);
				Bukkit.addRecipe(new FurnaceRecipe(vanillaKey, new ItemStack(Material.BARRIER), recipe.getIngredientMaterial(), 0, recipe.getCookingTime()));
			}
		}
	}

	@EventHandler
	public void a(FurnaceSmeltEvent event) {
		NBTItem ingredient = NBTItem.get(event.getSource());
		SmeltingRecipe recipe = getCorrespondingRecipe(ingredient);
		if (recipe == null)
			event.setCancelled(true);
		else
			event.setResult(recipe.getResult());
	}

	private SmeltingRecipe getCorrespondingRecipe(NBTItem item) {
		for (SmeltingRecipe recipe : recipes)
			if (recipe.matchesIngredient(item))
				return recipe;
		return null;
	}

	public class SmeltingRecipe {

		// ingredient & result
		private Type ingredientType, resultType;
		private String ingredientId, resultId;
		private Material ingredientMaterial;

		private int time;

		public SmeltingRecipe(ConfigurationSection section) {
			try {
				String[] split = section.getString("ingredient").split("\\.");
				ingredientMaterial = MMOItems.plugin.getItems().getItem(ingredientType = MMOItems.plugin.getTypes().get(split[0]), ingredientId = split[1]).getType();

				split = section.getString("result").split("\\.");
				resultType = MMOItems.plugin.getTypes().get(split[0]);
				resultId = split[1];

				time = (int) (section.getDouble("cook-time") * 20.);
			} catch (Exception e) {
				MMOCore.plugin.getLogger().log(Level.WARNING, "Could not load furnace recipe named " + section.getName());
			}
		}

		public SmeltingRecipe(Type ingredientType, String ingredientId, int time, Type resultType, String resultId) {
			this.ingredientType = ingredientType;
			this.ingredientId = ingredientId;
			this.time = time;
			this.resultType = resultType;
			this.resultId = resultId;
		}

		public boolean isValid() {
			return ingredientType != null && ingredientId != null && time > 0 && resultType != null && resultId != null && ingredientMaterial != null;
		}

		public boolean matchesIngredient(NBTItem nbt) {
			return nbt.getString("MMOITEMS_ITEM_TYPE").equals(ingredientType.getId()) && nbt.getString("MMOITEMS_ITEM_ID").equals(ingredientId);
		}

		@Deprecated
		public boolean matchesIngredient(ItemStack item) {
			return matchesIngredient(NBTItem.get(item));
		}

		public int getCookingTime() {
			return time;
		}

		public ItemStack getResult() {
			return MMOItems.plugin.getItems().getItem(resultType, resultId);
		}

		public Material getIngredientMaterial() {
			return ingredientMaterial;
		}

		public String toString() {
			return "{ingredient=" + ingredientType.getId() + "." + ingredientId + ", time" + time + ", result=" + resultType.getId() + "." + resultId + ", valid=" + isValid() + "}";
		}
	}
}
