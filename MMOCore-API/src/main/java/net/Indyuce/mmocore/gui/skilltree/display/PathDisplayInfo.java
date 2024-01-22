package net.Indyuce.mmocore.gui.skilltree.display;

import java.util.Objects;

public class PathDisplayInfo implements DisplayInfo{

    private PathType pathType;
    private PathStatus pathStatus;

    public PathDisplayInfo(PathType pathType, PathStatus pathStatus) {
        this.pathType = pathType;
        this.pathStatus = pathStatus;
    }

    public PathType getPathType() {
        return pathType;
    }

    public PathStatus getPathStatus() {
        return pathStatus;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pathType, pathStatus);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PathDisplayInfo that = (PathDisplayInfo) o;
        return pathType == that.pathType && pathStatus == that.pathStatus;
    }

    @Override
    public String toString() {
        return "PathDisplayInfo{" +
                "pathType=" + pathType +
                ", pathStatus=" + pathStatus +
                '}';
    }
}
