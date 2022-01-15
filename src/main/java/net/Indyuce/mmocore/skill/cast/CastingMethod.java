package net.Indyuce.mmocore.skill.cast;

import net.Indyuce.mmocore.skill.cast.listener.SkillBar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;

import java.util.function.Function;

public enum CastingMethod {

    /**
     * The first ever casting method to be implemented in MMOCore.
     * <p>
     * When pressing a key, the list of bound skills display on the
     * action bar
     */
    SKILL_BAR(config-> new SkillBar(config)),

    SKILL_SCROLL;

    /**
     * Initialize your skill combo by pressing some key
     */
    KEY_COMBOS(),

    /**
     * Not implemented yet.
     * <p>
     * This would allow players to cast skills by opening
     * a book with all the skills displayed into it and click
     * some clickable text to cast the skill.
     */
    SPELL_BOOK(),

    /**
     * Not implemented yet.
     * <p>
     * Much like the spell book but using a custom GUI instead
     * of a spell book to display the available skills.
     */
    SPELL_GUI();

    private final Function<ConfigurationSection, Listener> listenerLoader;

    CastingMethod(Function<ConfigurationSection, Listener> listenerLoader) {
        this.listenerLoader = listenerLoader;
    }
}
