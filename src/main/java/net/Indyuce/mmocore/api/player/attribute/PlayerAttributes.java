package net.Indyuce.mmocore.api.player.attribute;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.stats.stat.modifier.StatModifier;

public class PlayerAttributes {
	private final PlayerData data;
	private final Map<String, AttributeInstance> extra = new HashMap<>();

	public PlayerAttributes(PlayerData data) {
		this.data = data;
	}

	public void load(ConfigurationSection config) {
		for (String key : config.getKeys(false))
			try {
				String id = key.toLowerCase().replace("_", "-").replace(" ", "-");
				Validate.isTrue(MMOCore.plugin.attributeManager.has(id), "Could not find attribute '" + id + "'");

				PlayerAttribute attribute = MMOCore.plugin.attributeManager.get(id);
				AttributeInstance ins = new AttributeInstance(attribute);
				ins.setBase(config.getInt(key));
				extra.put(id, ins);
			} catch (IllegalArgumentException exception) {
				data.log(Level.WARNING, exception.getMessage());
			}
	}

	public void save(ConfigurationSection config) {
		extra.values().forEach(ins -> config.set(ins.id, ins.getBase()));
	}

	public PlayerData getData() {
		return data;
	}

	public int getAttribute(PlayerAttribute attribute) {
		return getInstance(attribute).getTotal();
	}

	public Collection<AttributeInstance> getAttributeInstances() {
		return extra.values();
	}

	public AttributeInstance getInstance(PlayerAttribute attribute) {
		if (extra.containsKey(attribute.getId()))
			return extra.get(attribute.getId());

		AttributeInstance ins = new AttributeInstance(attribute);
		extra.put(attribute.getId(), ins);
		return ins;
	}

	public int countSkillPoints() {
		int n = 0;
		for (AttributeInstance ins : extra.values())
			n += ins.getBase();
		return n;
	}

	public class AttributeInstance {
		private int spent;

		private final String id;
		private final Map<String, StatModifier> map = new HashMap<>();

		public AttributeInstance(PlayerAttribute attribute) {
			id = new String(attribute.getId());
		}

		public int getBase() {
			return spent;
		}

		public void setBase(int value) {
			spent = Math.max(0, value);

			update();
		}

		public void addBase(int value) {
			setBase(spent + value);
		}

		public int getTotal() {
			return (int) getTotal(spent);
		}

		/*
		 * 1) two types of attributes: flat attributes which add X to the value,
		 * and relative attributes which add X% and which must be applied
		 * afterwards 2) the 'd' parameter lets you choose if the relative
		 * attributes also apply on the base stat, or if they only apply on the
		 * extra stat value
		 */
		public double getTotal(double d) {

			for (StatModifier attr : map.values())
				if (attr.isRelative())
					d = attr.apply(d);

			for (StatModifier attr : map.values())
				if (!attr.isRelative())
					d = attr.apply(d);

			return d;
		}

		public StatModifier getModifier(String key) {
			return map.get(key);
		}

		public void addModifier(String key, double value) {
			addModifier(key, new StatModifier(value));
		}

		public void addModifier(String key, StatModifier modifier) {
			map.put(key, modifier);

			update();
		}

		public Set<String> getKeys() {
			return map.keySet();
		}

		public boolean contains(String key) {
			return map.containsKey(key);
		}

		public void remove(String key) {

			/*
			 * closing stat is really important with temporary stats because
			 * otherwise the runnable will try to remove the key from the map
			 * even though the attribute was cancelled before hand
			 */
			if (map.containsKey(key)) {
				map.get(key).close();
				map.remove(key);
			}

			update();
		}

		public void update() {
			PlayerAttribute attribute = MMOCore.plugin.attributeManager.get(id);
			int total = getTotal();
			attribute.getStats().forEach(stat -> data.getStats().getInstance(stat).addModifier("attribute." + attribute.getId(), attribute.getBuff(stat).multiply(total)));
		}

		public String getId() {
			return id;
		}
	}

	public void setBaseAttribute(String id, int value) {
		getAttributeInstances().forEach(ins -> {
			if(ins.getId().equals(id))
				ins.setBase(value);
		});
	}
}
