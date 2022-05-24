package net.Indyuce.mmocore.tree.skilltree;

import net.Indyuce.mmocore.tree.IntegerCoordinates;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class CustomSkillTree extends SkillTree{
    public CustomSkillTree(ConfigurationSection config) {
        super(config);
    }

    @Override
    protected void whenPostLoaded(@NotNull ConfigurationSection configurationSection) {

    }

    @Override
    public boolean isPath(IntegerCoordinates coordinates) {
        return false;
    }
}
