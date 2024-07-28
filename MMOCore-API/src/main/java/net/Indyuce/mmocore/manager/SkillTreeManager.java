package net.Indyuce.mmocore.manager;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.manager.registry.MMOCoreRegister;
import net.Indyuce.mmocore.skilltree.ParentType;
import net.Indyuce.mmocore.skilltree.SkillTreeNode;
import net.Indyuce.mmocore.skilltree.tree.SkillTree;
import net.Indyuce.mmocore.util.FileUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;


public class SkillTreeManager extends MMOCoreRegister<SkillTree> {
    private final HashMap<String, SkillTreeNode> skillTreeNodes = new HashMap<>();

    @Override
    public void register(SkillTree tree) {
        super.register(tree);
        tree.getNodes().forEach((node) -> skillTreeNodes.put(node.getFullId(), node));
    }

    public boolean has(int index) {
        return index >= 0 && index < registered.values().stream().collect(Collectors.toList()).size();
    }

    public SkillTreeNode getNode(String fullId) {
        return skillTreeNodes.get(fullId);
    }

    /**
     * Useful to recursively go trough trees
     *
     * @return The list of all the roots (e.g the nodes without any parents
     */
    public List<SkillTreeNode> getRootNodes() {
        return skillTreeNodes.values().stream().filter(treeNode -> treeNode.getParents(ParentType.SOFT).size() == 0).collect(Collectors.toList());
    }

    public Collection<SkillTreeNode> getAllNodes() {
        return skillTreeNodes.values();
    }

    public SkillTree get(int index) {
        return registered.values().stream().collect(Collectors.toList()).get(index);
    }

    @Override

    public String getRegisteredObjectName() {
        return "skill tree";
    }


    @Override
    public void initialize(boolean clearBefore) {
        if (clearBefore) registered.clear();

        FileUtils.loadObjectsFromFolder(MMOCore.plugin, "skill-trees", true, (key, config) -> {
            SkillTree skillTree = SkillTree.loadSkillTree(config);
            if (skillTree != null) register(skillTree);
        }, "Could not load skill tree from file '%s': %s");
    }
}
