package net.Indyuce.mmocore.skilltree.tree.display;

import net.Indyuce.mmocore.skilltree.NodeStatus;

import java.util.Objects;

/**
 * The information needed to determine the display type of a node
 */
public class DisplayInfo {
    private NodeStatus nodeStatus;
    private  int size;

    //this NodeDisplayInfo represent a path
    public static DisplayInfo pathInfo= new DisplayInfo();


    public DisplayInfo() {
    }

    public DisplayInfo(NodeStatus nodeStatus, int size) {
        this.nodeStatus = nodeStatus;
        this.size = size;
    }

    public NodeStatus getNodeState() {
        return nodeStatus;
    }

    public int getSize() {
        return size;
    }


    @Override
    public int hashCode() {
        return Objects.hash(nodeStatus, size);
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof DisplayInfo))
            return false;
        DisplayInfo displayInfo= (DisplayInfo) obj;
        if(nodeStatus ==null)
            return displayInfo.getNodeState()==null;
        return nodeStatus ==displayInfo.getNodeState()&&size==displayInfo.getSize();
    }

    @Override
    public String toString() {
        return "DisplayInfo{" +
                "nodeState=" + nodeStatus +
                ", size=" + size +
                '}';
    }
}
