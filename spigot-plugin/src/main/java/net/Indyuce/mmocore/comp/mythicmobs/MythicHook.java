package net.Indyuce.mmocore.comp.mythicmobs;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.events.MythicDropLoadEvent;
import io.lumine.mythic.bukkit.events.MythicReloadedEvent;
import io.lumine.mythic.core.skills.placeholders.Placeholder;
import net.Indyuce.mmocore.comp.mythicmobs.load.CurrencyItemDrop;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.attribute.PlayerAttribute;
import net.Indyuce.mmocore.api.player.attribute.PlayerAttributes;
import net.Indyuce.mmocore.comp.mythicmobs.load.GoldPouchDrop;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class MythicHook implements Listener {
    public MythicHook() {
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

    @EventHandler(priority = EventPriority.HIGH)
    public void b(MythicReloadedEvent event) {

        // When MM is reloaded, reload placeholders because they are no longer registered
        registerPlaceholders();

        // Reload every god damn thing
        MMOCore.plugin.initializePlugin(true);
    }

    private void registerPlaceholders() {

        // Resource
        MythicBukkit.inst().getPlaceholderManager().register("mana", Placeholder.meta((metadata, arg) -> {
            return String.valueOf((int) PlayerData.get(metadata.getCaster().getEntity().getUniqueId()).getMana());
        }));
        MythicBukkit.inst().getPlaceholderManager().register("stamina", Placeholder.meta((metadata, arg) -> {
            return String.valueOf((int) PlayerData.get(metadata.getCaster().getEntity().getUniqueId()).getStamina());
        }));
        MythicBukkit.inst().getPlaceholderManager().register("stellium", Placeholder.meta((metadata, arg) -> {
            return String.valueOf((int) PlayerData.get(metadata.getCaster().getEntity().getUniqueId()).getStellium());
        }));

        // Attributes
        MythicBukkit.inst().getPlaceholderManager().register("attribute", Placeholder.meta((metadata, arg) -> {
            PlayerAttributes attributes = PlayerData.get(metadata.getCaster().getEntity().getUniqueId()).getAttributes();
            PlayerAttribute attribute = MMOCore.plugin.attributeManager.get(arg);
            return String.valueOf(attributes.getAttribute(attribute));
        }));

    }
}