package com.codingforcookies.armorequip;

import java.util.function.Function;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import net.mmogroup.mmolib.version.VersionMaterial;

/**
 * @author Arnah
 * @since Jul 30, 2015
 */
public enum ArmorType {
	HELMET(5, (inv) -> inv.getHelmet()),
	CHESTPLATE(6, (inv) -> inv.getChestplate()),
	LEGGINGS(7, (inv) -> inv.getLeggings()),
	BOOTS(8, (inv) -> inv.getBoots());

	private final int slot;
	private final Function<PlayerInventory, ItemStack> handler;

	private ArmorType(int slot, Function<PlayerInventory, ItemStack> handler) {
		this.slot = slot;
		this.handler = handler;
	}

	public int getSlot() {
		return slot;
	}

	public ItemStack getItem(Player player) {
		return handler.apply(player.getInventory());
	}

	/**
	 * Attempts to match the ArmorType for the specified ItemStack.
	 *
	 * @param itemStack
	 *            The ItemStack to parse the type of.
	 * @return The parsed ArmorType. (null if none were found.)
	 */
	public static ArmorType matchType(ItemStack item) {
		if (item == null || item.getType().equals(Material.AIR))
			return null;

		Material type = item.getType();
		String name = type.name();
		if (name.endsWith("HELMET") || name.endsWith("SKULL") || name.endsWith("HEAD") || type == VersionMaterial.PLAYER_HEAD.toMaterial() || type == Material.PUMPKIN)
			return HELMET;

		else if (name.endsWith("CHESTPLATE"))
			return CHESTPLATE;

		else if (name.endsWith("LEGGINGS"))
			return LEGGINGS;

		else if (name.endsWith("BOOTS"))
			return BOOTS;

		else
			return null;
	}
}