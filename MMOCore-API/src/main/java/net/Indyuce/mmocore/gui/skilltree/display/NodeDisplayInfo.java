package net.Indyuce.mmocore.gui.skilltree.display;

import net.Indyuce.mmocore.skilltree.NodeStatus;

import java.util.Objects;

public class NodeDisplayInfo implements DisplayInfo{

    private NodeStatus nodeStatus;
    private NodeType nodeType;

    public NodeDisplayInfo(NodeType nodeType,NodeStatus nodeStatus) {
        this.nodeStatus = nodeStatus;
        this.nodeType = nodeType;
    }

    public NodeStatus getNodeState() {
        return nodeStatus;
    }

    public NodeStatus getNodeStatus() {
        return nodeStatus;
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeDisplayInfo that = (NodeDisplayInfo) o;
        return nodeStatus == that.nodeStatus && nodeType == that.nodeType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeStatus, nodeType);
    }

    @Override
    public String toString() {
        return "NodeDisplayInfo{" +
                "nodeStatus=" + nodeStatus +
                ", nodeType=" + nodeType +
                '}';
    }
}
