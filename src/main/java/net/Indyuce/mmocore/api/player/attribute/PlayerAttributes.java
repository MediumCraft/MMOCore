package net.Indyuce.mmocore.api.player.attribute;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.player.modifier.Closeable;
import io.lumine.mythic.lib.player.modifier.ModifierSource;
import io.lumine.mythic.lib.player.modifier.ModifierType;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;

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

	public AttributeInstance getInstance(String attribute) {
		if (instances.containsKey(attribute))
			return instances.get(attribute);

		AttributeInstance ins = new AttributeInstance(attribute);
		instances.put(attribute, ins);
		return ins;
	}

	public AttributeInstance getInstance(PlayerAttribute attribute) {
		return getInstance(attribute.getId());
	}

	public int countSkillPoints() {
		int n = 0;
		for (AttributeInstance ins : instances.values())
			n += ins.getBase();
		return n;
	}

	public class AttributeInstance {
		private int spent;

		private final String id;
		private final Map<String, AttributeModifier> map = new HashMap<>();

		public AttributeInstance(String attribute) {
			id = attribute;
		}

		public int getBase() {
			return spent;
		}

		public void setBase(int value) {
			spent = Math.max(0, value);

			update();
		}

		/**
		 * Adds X points to the base of the player attribute AND applies
		 * the attribute experience table.
		 *
		 * @param value Amount of attribute points spent in the attribute
		 */
		public void addBase(int value) {
			PlayerAttribute attribute = MMOCore.plugin.attributeManager.get(id);
			setBase(spent + value);
			if (attribute.hasExperienceTable())
				attribute.getExperienceTable().claim(data, spent, attribute);
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

			for (AttributeModifier attr : map.values())
				if (attr.getType() == ModifierType.RELATIVE)
					d *= attr.getValue();

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
			AttributeModifier mod = map.put(modifier.getKey(), modifier);
			update();
			return mod;
		}

		public Set<String> getKeys() {
			return map.keySet();
		}

		public boolean contains(String key) {
			return map.containsKey(key);
		}

		public AttributeModifier removeModifier(String key) {
			AttributeModifier mod = map.remove(key);

			/*
			 * Closing stat is really important with temporary stats because
			 * otherwise the runnable will try to remove the key from the map
			 * even though the attribute was cancelled before hand
			 */
			if (mod != null) {
				if (mod instanceof Closeable)
					((Closeable) mod).close();
				update();
			}
			return mod;
		}

		public void update() {
			PlayerAttribute attr = MMOCore.plugin.attributeManager.get(id);
			int total = getTotal();
			attr.getBuffs().forEach(buff -> buff.multiply(total).register(data.getMMOPlayerData()));
		}

		public String getId() {
			return id;
		}
	}

	public void setBaseAttribute(String id, int value) {
		getInstances().forEach(ins -> {
			if (ins.getId().equals(id))
				ins.setBase(value);
		});
	}
}
