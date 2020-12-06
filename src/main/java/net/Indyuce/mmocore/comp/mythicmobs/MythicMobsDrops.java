package net.Indyuce.mmocore.comp.mythicmobs;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicDropLoadEvent;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicReloadedEvent;
import io.lumine.xikage.mythicmobs.skills.placeholders.Placeholder;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.comp.mythicmobs.load.CurrencyItemDrop;
import net.Indyuce.mmocore.comp.mythicmobs.load.GoldPouchDrop;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MythicMobsDrops implements Listener {
	public MythicMobsDrops() {
		registerPlaceholders();
	}

	@EventHandler
	public void a(MythicDropLoadEvent event) {

		// random gold pouches
		if (event.getDropName().equalsIgnoreCase("gold_pouch") || event.getDropName().equalsIgnoreCase("goldpouch"))
			event.register(new GoldPouchDrop(event.getConfig()));

		// gold coins
		if (event.getDropName().equalsIgnoreCase("gold_coin") || event.getDropName().equalsIgnoreCase("coin"))
			event.register(new CurrencyItemDrop("GOLD_COIN", event.getConfig()));

		// notes
		if (event.getDropName().equalsIgnoreCase("note") || event.getDropName().equalsIgnoreCase("banknote") || event.getDropName().equalsIgnoreCase("bank_note"))
			event.register(new CurrencyItemDrop("NOTE", event.getConfig()));
	}

	/*
	 * when MythicMobs is reloaded, the placeholders are emptied. Add them again
	 */
	@EventHandler
	public void b(MythicReloadedEvent event) {
		registerPlaceholders();
	}

	private void registerPlaceholders() {
		MythicMobs.inst().getPlaceholderManager().register("mmocore.skill", Placeholder.meta((metadata, arg) -> String.valueOf(PlayerData.get(metadata.getCaster().getEntity().getUniqueId()).getSkillData().getCachedModifier(arg))));
		MythicMobs.inst().getPlaceholderManager().register("mmocore.mana", Placeholder.meta((metadata, arg) -> String.valueOf((int) PlayerData.get(metadata.getCaster().getEntity().getUniqueId()).getMana())));
		MythicMobs.inst().getPlaceholderManager().register("mmocore.stamina", Placeholder.meta((metadata, arg) -> String.valueOf((int) PlayerData.get(metadata.getCaster().getEntity().getUniqueId()).getStamina())));
	}
}