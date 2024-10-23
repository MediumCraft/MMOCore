package net.Indyuce.mmocore.skill.cast;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.skill.cast.handler.KeyCombos;
import net.Indyuce.mmocore.skill.cast.handler.SkillBar;
import net.Indyuce.mmocore.skill.cast.handler.SkillCastingDisabled;
import net.Indyuce.mmocore.skill.cast.handler.SkillScroller;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
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
    SKILL_SCROLLER(config -> new SkillScroller(config)),

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
    NONE(config -> new SkillCastingDisabled(config));

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

    private final Function<ConfigurationSection, SkillCastingHandler> listenerLoader;

    private static SkillCastingHandler current;

    SkillCastingMode(Function<ConfigurationSection, SkillCastingHandler> listenerLoader) {
        this.listenerLoader = listenerLoader;
    }

    public void setCurrent(@NotNull ConfigurationSection config) {
        Validate.isTrue(current == null, "Skill casting mode already initialized");
        current = listenerLoader.apply(config);
        Bukkit.getPluginManager().registerEvents(current, MMOCore.plugin);
    }

    @NotNull
    public static SkillCastingHandler getInstance() {
        return Objects.requireNonNull(current, "Skill casting mode hasn't been initialized yet");
    }

    /**
     * @see #getInstance()
     * @deprecated
     */
    @Deprecated
    public static SkillCastingHandler getCurrent() {
        return getInstance();
    }
}
