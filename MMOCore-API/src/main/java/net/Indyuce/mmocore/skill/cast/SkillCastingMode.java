package net.Indyuce.mmocore.skill.cast;

import net.Indyuce.mmocore.skill.cast.listener.SkillBar;
import net.Indyuce.mmocore.skill.cast.listener.KeyCombos;
import net.Indyuce.mmocore.skill.cast.listener.SkillScroller;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;

import java.util.function.Function;

public enum SkillCastingMode {

    /**
     * The first ever casting method to be implemented in MMOCore.
     * <p>
     * When pressing a key, the list of bound skills display on the
     * action bar
     */
    SKILL_BAR(config -> new SkillBar(config)),

    /**
     * When entering casting mode you can use the mouse scroller
     * to navigate through the entire castable skill list. Then press
     * one key to cast the one selected.
     */
    SKILL_SCROLL(config -> new SkillScroller(config)),

    /**
     * Initialize your skill combo by pressing some key.
     * <p>
     * Then press a certain key combo. The config can be used
     * to map key combos to skill bind slots, for instance LLR
     * would cast the 2nd skill but LRL the 3rd one.
     */
    KEY_COMBOS(config -> new KeyCombos(config)),

    /**
     * Entirely disables skill casting.
     */
    NONE(config -> new Listener() {
    });

    /**
     * Not implemented yet.
     * <p>
     * This would allow players to cast skills by opening
     * a book with all the skills displayed into it and click
     * some clickable text to cast the skill.
     */
    // SPELL_BOOK(null),

    /**
     * Not implemented yet.
     * <p>
     * Much like the spell book but using a custom GUI instead
     * of a spell book to display the available skills.
     */
    // SPELL_GUI(null),

    ;

    private final Function<ConfigurationSection, Listener> listenerLoader;

    SkillCastingMode(Function<ConfigurationSection, Listener> listenerLoader) {
        this.listenerLoader = listenerLoader;
    }

    public Listener loadFromConfig(ConfigurationSection config) {
        return listenerLoader.apply(config);
    }
}
