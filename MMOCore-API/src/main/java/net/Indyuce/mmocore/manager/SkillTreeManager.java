package net.Indyuce.mmocore.manager;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.tree.skilltree.SkillTree;
import net.Indyuce.mmocore.manager.registry.MMOCoreRegister;
import net.Indyuce.mmocore.tree.SkillTreeNode;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;
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
        return skillTreeNodes.values().stream().filter(treeNode -> treeNode.getSoftParents().size() == 0).collect(Collectors.toList());
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
        if (clearBefore)
            registered.clear();
        File file = new File(MMOCore.plugin.getDataFolder() + "/skilltree");
        if (!file.exists())
            file.mkdirs();
        load(file);
    }


    public void load(File file) {
        if (file.isDirectory()) {
            List<File> fileList = Arrays.asList(file.listFiles()).stream().sorted().collect(Collectors.toList());
            for (File child : fileList) {
                load(child);
            }
        } else {
            SkillTree skillTree = SkillTree.loadSkillTree(YamlConfiguration.loadConfiguration(file));
            if (skillTree != null)
                register(skillTree);
        }
    }
}
