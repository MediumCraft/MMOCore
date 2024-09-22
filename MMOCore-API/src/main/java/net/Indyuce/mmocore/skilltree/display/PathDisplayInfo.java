package net.Indyuce.mmocore.skilltree.display;

import net.Indyuce.mmocore.skilltree.PathType;
import net.Indyuce.mmocore.skilltree.NodeState;

import java.util.Objects;

public class PathDisplayInfo extends DisplayInfo {
    private final PathType type;

    public PathDisplayInfo(PathType type, NodeState status) {
        super(status);

        this.type = type;
    }

    public PathType getType() {
        return type;
    }

    public NodeState getStatus() {
        return state;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, state);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PathDisplayInfo that = (PathDisplayInfo) o;
        return type == that.type && state == that.state;
    }

    @Override
    public String toString() {
        return "PathDisplayInfo{" + "type=" + type + ", status=" + state + '}';
    }
}
