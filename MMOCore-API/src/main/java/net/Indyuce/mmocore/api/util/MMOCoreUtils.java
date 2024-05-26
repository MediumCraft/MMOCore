package net.Indyuce.mmocore.api.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.hologram.Hologram;
import io.lumine.mythic.lib.version.VersionMaterial;
import net.Indyuce.mmocore.MMOCore;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;

public class MMOCoreUtils {
    public static boolean pluginItem(ItemStack item) {
        return item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName();
    }

    /**
     * If a given player is not in the server cache, no information
     * cannot be retrieved from that player (without using requests
     * to MC servers obviously). In that case, the instance of
     * OfflinePlayer is pretty much useless and it only wraps its
     * UUID which was already known beforehand.
     *
     * @param player Offline player instance to test
     * @return Is the instance valid
     */
    public static boolean isInvalid(OfflinePlayer player) {
        return player.getName() == null;
    }

    public static String displayName(ItemStack item) {
        return item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName()
                : UtilityMethods.caseOnWords(item.getType().name().replace("_", " "));
    }

    /**
     * @param current Current value of resource
     * @param maxStat Maximum value of resource
     * @return Clamped resource value. If the provided current value is 0,
     * this function will return the maximum resource value.
     */
    public static double fixResource(double current, double maxStat) {
        return current == 0 ? maxStat : Math.max(0, Math.min(current, maxStat));
    }

    @Deprecated
    public static String caseOnWords(String s) {
        StringBuilder builder = new StringBuilder(s);
        boolean isLastSpace = true;
        for (int item = 0; item < builder.length(); item++) {
            char ch = builder.charAt(item);
            if (isLastSpace && ch >= 'a' && ch <= 'z') {
                builder.setCharAt(item, (char) (ch + ('A' - 'a')));
                isLastSpace = false;
            } else
                isLastSpace = ch == ' ';
        }
        return builder.toString();
    }

    public static String ymlName(String str) {
        return str.toLowerCase().replace("_", "-").replace(" ", "-");
    }

    /**
     * @param value an integer you want to convert
     * @return the string representing the integer but with roman letters
     */
    public static String toRomanNumerals(int value) {
        LinkedHashMap<String, Integer> roman_numerals = new LinkedHashMap<String, Integer>();
        roman_numerals.put("M", 1000);
        roman_numerals.put("CM", 900);
        roman_numerals.put("D", 500);
        roman_numerals.put("CD", 400);
        roman_numerals.put("C", 100);
        roman_numerals.put("XC", 90);
        roman_numerals.put("L", 50);
        roman_numerals.put("XL", 40);
        roman_numerals.put("X", 10);
        roman_numerals.put("IX", 9);
        roman_numerals.put("V", 5);
        roman_numerals.put("IV", 4);
        roman_numerals.put("I", 1);
        String res = "";
        for (Map.Entry<String, Integer> entry : roman_numerals.entrySet()) {
            int matches = value / entry.getValue();
            res += repeat(entry.getKey(), matches);
            value = value % entry.getValue();
        }
        return res;
    }

    private static String repeat(String s, int n) {
        if (s == null) {
            return null;
        }
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append(s);
        }
        return sb.toString();
    }

    /**
     * Displays an in game indicator using a hologram. This uses
     * LumineUtils hologramFactory to summon holograms
     * <p>
     * The hologram despawns after 1sec
     *
     * @param loc     Target location
     * @param message Message to display
     */
    public static void displayIndicator(Location loc, String message) {
        Hologram holo = Hologram.create(loc, MythicLib.plugin.parseColors(Collections.singletonList(message)));
        Bukkit.getScheduler().runTaskLater(MMOCore.plugin, holo::despawn, 20);
    }

    public static boolean isPlayerHead(Material material) {
        return material == VersionMaterial.PLAYER_HEAD.toMaterial() || material == VersionMaterial.PLAYER_WALL_HEAD.toMaterial();
    }

    public static ItemStack readIcon(String string) throws IllegalArgumentException {
        String[] split = string.split(":");
        Material material = Material.valueOf(split[0].toUpperCase().replace("-", "_").replace(" ", "_"));
        return split.length > 1 ? MythicLib.plugin.getVersion().getWrapper().textureItem(material, Integer.parseInt(split[1])) : new ItemStack(material);
    }

    public static int getWorth(ItemStack[] items) {
        int t = 0;
        for (ItemStack item : items)
            if (item != null && item.getType() != Material.AIR)
                t += MythicLib.plugin.getVersion().getWrapper().getNBTItem(item).getInteger("RpgWorth") * item.getAmount();
        return t;
    }

    public static String toBase64(ItemStack[] items) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeInt(items.length);
            for (ItemStack item : items)
                dataOutput.writeObject(item);
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

    private static final String[] romanChars = {"I", "IV", "V", "IX", "X", "XL", "L", "XC", "C", "CD", "D", "CM", "M"};
    private static final int[] romanQuantities = {1, 4, 5, 9, 10, 40, 50, 90, 100, 400, 500, 900, 1000};

    public static String intToRoman(int input) {
        if (input < 1)
            return "<1";
        if (input > 3999)
            return ">3999";

        StringBuilder result = new StringBuilder();

        for (int j = 0; j < romanChars.length; j++) {
            int i = romanChars.length - j - 1;
            int q = romanQuantities[i];
            String c = romanChars[i];

            while (input >= q) {
                result.append(c);
                input -= q;
            }
        }

        return result.toString();
    }


    public static Collection<String> jsonArrayToList(String json) {
        return new ArrayList<>(Arrays.asList(MythicLib.plugin.getJson().parse(json, String[].class)));
    }

    public static String arrayToJsonString(Collection<String> array) {
        JsonArray object = new JsonArray();
        for (String str : array) {
            object.add(str);
        }
        return object.toString();
    }

    public static String entrySetToJsonString(Set<Map.Entry<String, Integer>> entrySet) {
        JsonObject object = new JsonObject();
        for (Map.Entry<String, Integer> entry : entrySet) {
            object.addProperty(entry.getKey(), entry.getValue());
        }
        return object.toString();
    }

    /**
     * Method to get all entities surrounding a location. This method does not
     * take every entity in the world but rather takes all the entities from the
     * 9 chunks around the entity, so even if the location is at the border of a
     * chunk (worst case border of 4 chunks), the entity will still be included
     */
    public static List<Entity> getNearbyChunkEntities(Location loc) {

        /*
         * Another method to save performance is: if an entity bounding box
         * calculation is made twice in the same tick, then the method does not
         * need to be called twice, it can utilize the same entity list since
         * the entities have not moved (e.g fireball which does 2+ calculations
         * per tick)
         *
         * Of course we're assuming that the projectile does move at a speed
         * lower than 1 chunk per second which is most likely true, otherwise
         * just use ray casting.
         */
        List<Entity> entities = new ArrayList<>();

        int cx = loc.getChunk().getX();
        int cz = loc.getChunk().getZ();

        for (int x = -1; x < 2; x++)
            for (int z = -1; z < 2; z++)
                entities.addAll(Arrays.asList(loc.getWorld().getChunkAt(cx + x, cz + z).getEntities()));

        return entities;
    }

    public static void heal(LivingEntity target, double value) {
        double max = target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        double gain = Math.min(max, target.getHealth() + value) - target.getHealth();

        EntityRegainHealthEvent event = new EntityRegainHealthEvent(target, gain, RegainReason.CUSTOM);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled())
            target.setHealth(target.getHealth() + gain);
    }

    private static final Random RANDOM = new Random();

    /**
     * Method used when mining a custom block or fishing, as the corresponding
     * interaction event is cancelled durability is not handled. This method is
     * needed and actually calls a damage event so that MMOItems can listen to it.
     * <p>
     * This method only supports item types which DO have a durability bar like
     * fishing rods or pickaxes. This shouldn't cause any issue because you can
     * only use fishing rods to fish and pickaxes to mine stuff.
     *
     * @param player Player holding the item with durability
     * @param slot   The slot of the item with durability
     * @param damage Damage that needs to be applied
     */
    public static void decreaseDurability(Player player, EquipmentSlot slot, int damage) {

        ItemStack item = player.getInventory().getItem(slot);
        if (item == null || item.getType().getMaxDurability() == 0 || item.getItemMeta().isUnbreakable())
            return;

        // Check unbreakable, ignore if necessary
        final ItemMeta meta = item.getItemMeta();
        final int unbreakingLevel = meta.getEnchantLevel(Enchantment.DURABILITY);
        if (unbreakingLevel > 0 && RANDOM.nextInt(unbreakingLevel + 1) != 0) return;

        PlayerItemDamageEvent event = new PlayerItemDamageEvent(player, item, damage);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return;

        final int newDamage = event.getDamage() + ((Damageable) meta).getDamage();
        if (newDamage >= item.getType().getMaxDurability()) {
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1F, 1F);
            player.getInventory().setItem(slot, null);
        } else {
            ((Damageable) meta).setDamage(newDamage);
            item.setItemMeta(meta);
        }
    }

    /**
     * @return Center location of an entity using its bounding box
     */
    public static Location getCenterLocation(Entity entity) {
        return entity.getBoundingBox().getCenter().toLocation(entity.getWorld());
    }

    public static void debug(String message) {
        message = ChatColor.YELLOW + "Debug> " + ChatColor.WHITE + message;
        for (Player player : Bukkit.getOnlinePlayers())
            player.sendMessage(message);
        Bukkit.getConsoleSender().sendMessage(message);
    }
}
