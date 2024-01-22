package net.Indyuce.mmocore.api.player.attribute;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.stat.StatInstance;
import io.lumine.mythic.lib.player.modifier.Closeable;
import io.lumine.mythic.lib.player.modifier.ModifierSource;
import io.lumine.mythic.lib.player.modifier.ModifierType;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;

public class PlayerAttributes {
    private final PlayerData data;
    private final Map<String, AttributeInstance> instances = new HashMap<>();

    public PlayerAttributes(PlayerData data) {
        this.data = data;
    }

    public void load(ConfigurationSection config) {
        for (String key : config.getKeys(false))
            try {
                String id = key.toLowerCase().replace("_", "-").replace(" ", "-");
                Validate.isTrue(MMOCore.plugin.attributeManager.has(id), "Could not find attribute '" + id + "'");

                PlayerAttribute attribute = MMOCore.plugin.attributeManager.get(id);
                AttributeInstance ins = new AttributeInstance(attribute.getId());
                ins.setBase(config.getInt(key));
                instances.put(id, ins);
            } catch (IllegalArgumentException exception) {
                data.log(Level.WARNING, exception.getMessage());
            }
    }

    public void save(ConfigurationSection config) {
        instances.values().forEach(ins -> config.set(ins.id, ins.getBase()));
    }

    public String toJsonString() {
        JsonObject json = new JsonObject();
        for (AttributeInstance ins : instances.values())
            json.addProperty(ins.getId(), ins.getBase());
        return json.toString();
    }

    public void load(String json) {
        Gson parser = new Gson();
        JsonObject jo = parser.fromJson(json, JsonObject.class);
        for (Entry<String, JsonElement> entry : jo.entrySet()) {
            try {
                String id = entry.getKey().toLowerCase().replace("_", "-").replace(" ", "-");
                Validate.isTrue(MMOCore.plugin.attributeManager.has(id), "Could not find attribute '" + id + "'");

                PlayerAttribute attribute = MMOCore.plugin.attributeManager.get(id);
                AttributeInstance ins = new AttributeInstance(attribute.getId());
                ins.setBase(entry.getValue().getAsInt());
                instances.put(id, ins);
            } catch (IllegalArgumentException exception) {
                data.log(Level.WARNING, exception.getMessage());
            }
        }
    }

    public PlayerData getData() {
        return data;
    }

    public int getAttribute(PlayerAttribute attribute) {
        return getInstance(attribute).getTotal();
    }

    public Collection<AttributeInstance> getInstances() {
        return instances.values();
    }

    public Map<String, Integer> mapPoints() {
        Map<String, Integer> map = new HashMap<>();
        instances.values().forEach(ins -> map.put(ins.id, ins.spent));
        return map;
    }

    @NotNull
    public AttributeInstance getInstance(String attribute) {
        return instances.computeIfAbsent(attribute, AttributeInstance::new);
    }

    @NotNull
    public AttributeInstance getInstance(PlayerAttribute attribute) {
        return getInstance(attribute.getId());
    }

    @Deprecated
    public int countSkillPoints() {
        return countPoints();
    }

    public int countPoints() {
        int n = 0;
        for (AttributeInstance ins : instances.values())
            n += ins.getBase();
        return n;
    }

    // TODO have it extend ModifiedInstance
    public class AttributeInstance {
        private int spent;

        private final String id, enumName;
        private final Map<String, AttributeModifier> map = new HashMap<>();

        public AttributeInstance(String id) {
            this.id = id;
            this.enumName = UtilityMethods.enumName(this.id);
        }

        public int getBase() {
            return spent;
        }

        @Deprecated
        public int getSpent() {
            return getBase();
        }

        public void setBase(int value) {
            spent = Math.max(0, value);

            if (data.isOnline())
                updateStats();
        }

        public void addBase(int value) {
            setBase(spent + value);
        }

        /*
         * 1) two types of attributes: flat attributes which add X to the value,
         * and relative attributes which add X% and which must be applied
         * afterwards 2) the 'd' parameter lets you choose if the relative
         * attributes also apply on the base stat, or if they only apply on the
         * instances stat value
         */
        public int getTotal() {
            double d = spent;

            for (AttributeModifier attr : map.values())
                if (attr.getType() == ModifierType.FLAT)
                    d += attr.getValue();

            d += data.getMMOPlayerData().getStatMap().getStat("ADDITIONAL_" + enumName);

            for (AttributeModifier attr : map.values())
                if (attr.getType() == ModifierType.RELATIVE)
                    d *= attr.getValue();

            d *= 1 + data.getMMOPlayerData().getStatMap().getStat("ADDITIONAL_" + enumName + "_PERCENT") / 100;

            // cast to int at the last moment
            return (int) d;
        }

        public AttributeModifier getModifier(String key) {
            return map.get(key);
        }

        public AttributeModifier addModifier(String key, double value) {
            return addModifier(new AttributeModifier(key, id, value, ModifierType.FLAT, EquipmentSlot.OTHER, ModifierSource.OTHER));
        }

        public AttributeModifier addModifier(AttributeModifier modifier) {
            final AttributeModifier current = map.put(modifier.getKey(), modifier);

            if (current != null && current instanceof Closeable)
                ((Closeable) current).close();

            updateStats();
            return current;
        }

        public Set<String> getKeys() {
            return map.keySet();
        }

        public boolean contains(String key) {
            return map.containsKey(key);
        }

        public AttributeModifier removeModifier(String key) {
            final AttributeModifier mod = map.remove(key);

            /*
             * Closing stat is really important with temporary stats because
             * otherwise the runnable will try to remove the key from the map
             * even though the attribute was cancelled before hand
             */
            if (mod != null) {
                if (mod instanceof Closeable)
                    ((Closeable) mod).close();
                updateStats();
            }
            return mod;
        }

        public void updateStats() {
            final PlayerAttribute attr = MMOCore.plugin.attributeManager.get(id);
            final int total = getTotal();

            // Remove ALL stat modifiers
            for (StatInstance ins : data.getMMOPlayerData().getStatMap().getInstances())
                ins.removeIf(str -> str.equals("attribute." + id));

            // Register new stat modifiers
            attr.getBuffs().forEach(buff -> buff.multiply(total).register(data.getMMOPlayerData()));
        }

        public String getId() {
            return id;
        }
    }

    @Deprecated
    public void setBaseAttribute(String id, int value) {
        AttributeInstance ins = instances.get(id);
        if (ins != null) ins.setBase(value);
    }
}
