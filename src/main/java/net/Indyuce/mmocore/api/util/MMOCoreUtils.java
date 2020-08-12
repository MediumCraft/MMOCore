package net.Indyuce.mmocore.api.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.version.VersionMaterial;

public class MMOCoreUtils {
	public static boolean pluginItem(ItemStack item) {
		return item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName();
	}

	public static String displayName(ItemStack item) {
		return item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName()
				: caseOnWords(item.getType().name().replace("_", " "));
	}

	public static String caseOnWords(String s) {
		StringBuilder builder = new StringBuilder(s);
		boolean isLastSpace = true;
		for (int item = 0; item < builder.length(); item++) {
			char ch = builder.charAt(item);
			if (isLastSpace && ch >= 'a' && ch <= 'z') {
				builder.setCharAt(item, (char) (ch + ('A' - 'a')));
				isLastSpace = false;
			} else if (ch != ' ')
				isLastSpace = false;
			else
				isLastSpace = true;
		}
		return builder.toString();
	}

	public static boolean isPlayerHead(Material material) {
		return material == VersionMaterial.PLAYER_HEAD.toMaterial() || material == VersionMaterial.PLAYER_WALL_HEAD.toMaterial();
	}

	public static ItemStack readIcon(String string) throws IllegalArgumentException {
		String[] split = string.split("\\:");
		Material material = Material.valueOf(split[0].toUpperCase().replace("-", "_").replace(" ", "_"));
		return split.length > 1 ? MMOLib.plugin.getVersion().getWrapper().textureItem(material, Integer.parseInt(split[1])) : new ItemStack(material);
	}

	public static int getWorth(ItemStack[] items) {
		int t = 0;
		for (ItemStack item : items)
			if (item != null && item.getType() != Material.AIR)
				t += MMOLib.plugin.getVersion().getWrapper().getNBTItem(item).getInteger("RpgWorth") * item.getAmount();
		return t;
	}

	public static String toBase64(ItemStack[] items) {
		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
			dataOutput.writeInt(items.length);
			for (int i = 0; i < items.length; i++)
				dataOutput.writeObject(items[i]);
			dataOutput.close();
			return Base64Coder.encodeLines(outputStream.toByteArray());
		} catch (Exception e) {
			return null;
		}
	}

	public static ItemStack[] itemStackArrayFromBase64(String data) {
		try {
			ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
			BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
			ItemStack[] items = new ItemStack[dataInput.readInt()];
			for (int i = 0; i < items.length; i++)
				items[i] = (ItemStack) dataInput.readObject();
			dataInput.close();
			return items;
		} catch (Exception e) {
			return null;
		}
	}

	private static final String[] romanChars = { "I", "IV", "V", "IX", "X", "XL", "L", "XC", "C", "CD", "D", "CM", "M" };
	private static final int[] romanQuantities = { 1, 4, 5, 9, 10, 40, 50, 90, 100, 400, 500, 900, 1000 };

	public static String intToRoman(int input) {
		if (input < 1)
			return "<1";
		if (input > 3999)
			return ">3999";

		String result = "";

		for (int j = 0; j < romanChars.length; j++) {
			int i = romanChars.length - j - 1;
			int q = romanQuantities[i];
			String c = romanChars[i];

			while (input >= q) {
				result += c;
				input -= q;
			}
		}

		return result;
	}

	/*
	 * method to get all entities surrounding a location. this method does not
	 * take every entity in the world but rather takes all the entities from the
	 * 9 chunks around the entity, so even if the location is at the border of a
	 * chunk (worst case border of 4 chunks), the entity will still be included
	 */
	public static List<Entity> getNearbyChunkEntities(Location loc) {

		/*
		 * another method to save performance is if an entity bounding box
		 * calculation is made twice in the same tick then the method does not
		 * need to be called twice, it can utilize the same entity list since
		 * the entities have not moved (e.g fireball which does 2+ calculations
		 * per tick)
		 */
		List<Entity> entities = new ArrayList<>();

		int cx = loc.getChunk().getX();
		int cz = loc.getChunk().getZ();

		for (int x = -1; x < 2; x++)
			for (int z = -1; z < 2; z++)
				for (Entity entity : loc.getWorld().getChunkAt(cx + x, cz + z).getEntities())
					entities.add(entity);

		return entities;
	}

	// TODO worldguard flags support for no target
	public static boolean canTarget(PlayerData player, Entity target) {

		// basic checks
		if (!(target instanceof LivingEntity) || player.getPlayer().equals(target) || target.isDead() || MMOLib.plugin.getEntities().findCustom(target))
			return false;

		// party check
		if (target instanceof Player) {
			PlayerData targetData = PlayerData.get((Player) target);
			if (targetData.hasParty() && targetData.getParty().getMembers().has(player))
				return false;
		}

		return true;
	}

	public static void heal(LivingEntity target, double value) {

		double max = target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
		double gain = Math.min(max, target.getHealth() + value) - target.getHealth();

		EntityRegainHealthEvent event = new EntityRegainHealthEvent(target, gain, RegainReason.CUSTOM);
		Bukkit.getPluginManager().callEvent(event);
		if (!event.isCancelled())
			target.setHealth(target.getHealth() + gain);
	}
}
