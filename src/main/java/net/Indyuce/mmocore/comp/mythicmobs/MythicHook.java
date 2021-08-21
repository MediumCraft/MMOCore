package net.Indyuce.mmocore.comp.mythicmobs;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicDropLoadEvent;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicReloadedEvent;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import io.lumine.xikage.mythicmobs.skills.placeholders.Placeholder;
import io.lumine.xikage.mythicmobs.skills.variables.Variable;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.comp.mythicmobs.load.CurrencyItemDrop;
import net.Indyuce.mmocore.comp.mythicmobs.load.GoldPouchDrop;
import org.bukkit.event.EventHandler;
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

    // When MythicMobs is reloaded, the placeholders are emptied. Add them again
    @EventHandler
    public void b(MythicReloadedEvent event) {
        registerPlaceholders();
    }

    private void registerPlaceholders() {

        MythicMobs.inst().getPlaceholderManager().register("modifier", Placeholder.meta((metadata, arg) -> {
            if (!(metadata instanceof SkillMetadata))
                throw new RuntimeException("Cannot use this placeholder outside of skill");

            Variable var = ((SkillMetadata) metadata).getVariables().get("MMOCoreSkill");
            net.Indyuce.mmocore.skill.metadata.SkillMetadata cast = (net.Indyuce.mmocore.skill.metadata.SkillMetadata) var.get();
            return String.valueOf(cast.getModifier(arg));
        }));

        MythicMobs.inst().getPlaceholderManager().register("modifier.int", Placeholder.meta((metadata, arg) -> {
            if (!(metadata instanceof SkillMetadata))
                throw new RuntimeException("Cannot use this placeholder outside of skill");

            Variable var = ((SkillMetadata) metadata).getVariables().get("MMOCoreSkill");
            net.Indyuce.mmocore.skill.metadata.SkillMetadata cast = (net.Indyuce.mmocore.skill.metadata.SkillMetadata) var.get();
            return String.valueOf((int) cast.getModifier(arg));
        }));

        MythicMobs.inst().getPlaceholderManager().register("mana", Placeholder.meta((metadata, arg) -> String.valueOf((int) PlayerData.get(metadata.getCaster().getEntity().getUniqueId()).getMana())));
        MythicMobs.inst().getPlaceholderManager().register("stamina", Placeholder.meta((metadata, arg) -> String.valueOf((int) PlayerData.get(metadata.getCaster().getEntity().getUniqueId()).getStamina())));
    }
}