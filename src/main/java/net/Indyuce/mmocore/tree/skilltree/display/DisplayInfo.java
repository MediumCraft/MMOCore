package net.Indyuce.mmocore.tree.skilltree.display;

import net.Indyuce.mmocore.tree.NodeState;

import java.util.Objects;

/**
 * The information needed to determine the display type of a node
 */
public class DisplayInfo {
    private  NodeState nodeState;
    private  int size;

    //this NodeDisplayInfo represent a path
    public static DisplayInfo pathInfo= new DisplayInfo();


    public DisplayInfo() {
    }

    public DisplayInfo(NodeState nodeState, int size) {
        this.nodeState = nodeState;
        this.size = size;
    }

    public NodeState getNodeState() {
        return nodeState;
    }

    public int getSize() {
        return size;
    }


    @Override
    public int hashCode() {
        return Objects.hash(nodeState, size);
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof DisplayInfo))
            return false;
        DisplayInfo displayInfo= (DisplayInfo) obj;
        if(nodeState==null)
            return displayInfo.getNodeState()==null;
        return nodeState==displayInfo.getNodeState()&&size==displayInfo.getSize();
    }

    @Override
    public String toString() {
        return "DisplayInfo{" +
                "nodeState=" + nodeState +
                ", size=" + size +
                '}';
    }
}
