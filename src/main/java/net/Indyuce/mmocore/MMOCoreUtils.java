package net.Indyuce.mmocore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
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

public class MMOCoreUtils {
	public static boolean pluginItem(ItemStack item) {
		return item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName();
	}

	public static String displayName(ItemStack item) {
		return item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : caseOnWords(item.getType().name().replace("_", " "));
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

	public static ItemStack readIcon(String string) {
		try {
			Validate.notNull(string, "String cannot be null");
			String[] split = string.split("\\:");
			Material material = Material.valueOf(split[0].toUpperCase().replace("-", "_").replace(" ", "_"));
			return split.length > 1 ? MMOCore.plugin.version.getTextureHandler().textureItem(material, Integer.parseInt(split[1])) : new ItemStack(material);
		} catch (IllegalArgumentException exception) {
			return new ItemStack(Material.BARRIER);
		}
	}

	public static int getWorth(ItemStack[] items) {
		int t = 0;
		for (ItemStack item : items)
			if (item != null && item.getType() != Material.AIR)
				t += MMOCore.plugin.nms.getNBTItem(item).getInt("RpgWorth") * item.getAmount();
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

	public static String intToRoman(int input) {
		if (input < 1 || input > 499)
			return ">499";

		String s = "";
		while (input >= 400) {
			s += "CD";
			input -= 400;
		}
		while (input >= 100) {
			s += "C";
			input -= 100;
		}
		while (input >= 90) {
			s += "XC";
			input -= 90;
		}
		while (input >= 50) {
			s += "L";
			input -= 50;
		}
		while (input >= 40) {
			s += "XL";
			input -= 40;
		}
		while (input >= 10) {
			s += "X";
			input -= 10;
		}
		while (input >= 9) {
			s += "IX";
			input -= 9;
		}
		while (input >= 5) {
			s += "V";
			input -= 5;
		}
		while (input >= 4) {
			s += "IV";
			input -= 4;
		}
		while (input >= 1) {
			s += "I";
			input -= 1;
		}
		return s;
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
	public static boolean canTarget(Player player, Entity target) {
		return !player.equals(target) && target instanceof LivingEntity && !target.isDead() && !MMOCore.plugin.entities.findCustom(target);
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
