package net.Indyuce.mmocore.skilltree.tree;

import net.Indyuce.mmocore.skilltree.ParentType;
import net.Indyuce.mmocore.skilltree.SkillTreeNode;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ParentInformation {
    private final SkillTreeNode node;
    private final ParentType type;

    public ParentInformation(SkillTreeNode node, ParentType type) {
        this.node = node;
        this.type = type;
    }

    @NotNull
    public SkillTreeNode getNode() {
        return node;
    }

    @NotNull
    public ParentType getType() {
        return type;
    }

    @Deprecated
    public ParentType type() {
        return type;
    }

    @Deprecated
    public SkillTreeNode node() {
        return node;
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
