package net.Indyuce.mmocore.gui.skilltree.display;

import net.Indyuce.mmocore.skilltree.SkillTreeStatus;

import java.util.Objects;

public class NodeDisplayInfo implements DisplayInfo{

    private SkillTreeStatus skillTreeStatus;
    private NodeType nodeType;

    public NodeDisplayInfo(NodeType nodeType, SkillTreeStatus skillTreeStatus) {
        this.skillTreeStatus = skillTreeStatus;
        this.nodeType = nodeType;
    }

    public SkillTreeStatus getNodeState() {
        return skillTreeStatus;
    }

    public SkillTreeStatus getNodeStatus() {
        return skillTreeStatus;
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeDisplayInfo that = (NodeDisplayInfo) o;
        return skillTreeStatus == that.skillTreeStatus && nodeType == that.nodeType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(skillTreeStatus, nodeType);
    }

    @Override
    public String toString() {
        return "NodeDisplayInfo{" +
                "nodeStatus=" + skillTreeStatus +
                ", nodeType=" + nodeType +
                '}';
    }
}
