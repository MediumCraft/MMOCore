package net.Indyuce.mmocore.skilltree.tree;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public enum SkillTreeType {

    /**
     * Fully custom skill tree with manual inputs on path placement
     */
    CUSTOM(CustomSkillTree::new),

    /**
     * A simpler skill tree pattern where neighbor nodes are instantly
     * soft parents of one another.
     */
    PROXIMITY(ProximitySkillTree::new),
    ;

    private final Function<ConfigurationSection, SkillTree> constructor;

    SkillTreeType(Function<ConfigurationSection, SkillTree> constructor) {
        this.constructor = constructor;
    }

    @NotNull
    public SkillTree construct(@NotNull ConfigurationSection config) {
        return constructor.apply(config);
    }
}
