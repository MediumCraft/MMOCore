package net.Indyuce.mmocore.skill;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.event.PlayerRegenResourceEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.skill.Skill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class Neptune_Gift extends Skill implements Listener {
	public Neptune_Gift() {
		super("NEPTUNE_GIFT");
		setName("Neptune's Gift");

		setMaterial(Material.LILY_PAD);
		setLore("Resource regeneration is increased by &8{extra} &7when standing in water.");
		setPassive();

		addModifier("extra", new LinearValue(30, 5));

		Bukkit.getPluginManager().registerEvents(this, MMOCore.plugin);
	}

	@EventHandler
	public void a(PlayerRegenResourceEvent event) {
		PlayerData data = event.getData();
		if (event.getPlayer().getLocation().getBlock().getType() == Material.WATER && data.hasSkillUnlocked(this))
			event.setAmount(event.getAmount() * (1 + data.getProfess().getSkill(this).getModifier("extra", data.getSkillLevel(this)) / 100));
	}
}
