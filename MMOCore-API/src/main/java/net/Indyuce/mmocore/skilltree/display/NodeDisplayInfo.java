package net.Indyuce.mmocore.skilltree.display;

import net.Indyuce.mmocore.skilltree.NodeType;
import net.Indyuce.mmocore.skilltree.NodeState;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class NodeDisplayInfo extends DisplayInfo {
    private final NodeType type;

    public NodeDisplayInfo(@NotNull NodeType type, @NotNull NodeState status) {
        super(status);

        this.type = type;
    }

    public NodeType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeDisplayInfo that = (NodeDisplayInfo) o;
        return state == that.state && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(state, type);
    }

    @Override
    public String toString() {
        return "NodeDisplayInfo{" + "status=" + state + ", type=" + type + '}';
    }
}
