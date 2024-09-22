package net.Indyuce.mmocore.skilltree;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ParentInformation {
    private final SkillTreeNode node;
    private final ParentType type;
    private final int level;

    public ParentInformation(SkillTreeNode node, ParentType type, int level) {
        this.node = node;
        this.type = type;
        this.level = level;
    }

    @NotNull
    public SkillTreeNode getNode() {
        return node;
    }

    @NotNull
    public ParentType getType() {
        return type;
    }

    public int getLevel() {
        return level;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParentInformation that = (ParentInformation) o;
        return Objects.equals(node, that.node) && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(node, type);
    }
}
