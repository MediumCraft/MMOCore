package net.Indyuce.mmocore.manager;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.gui.SkillTreeViewer;
import net.Indyuce.mmocore.manager.registry.MMOCoreRegister;
import net.Indyuce.mmocore.tree.SkillTreeNode;
import net.Indyuce.mmocore.tree.skilltree.SkillTree;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class SkillTreeManager extends MMOCoreRegister<SkillTree> {
    private final ArrayList<SkillTreeNode> skillTreeNodes = new ArrayList<>();

    @Override
    public void register(SkillTree tree){
        super.register(tree);
        tree.getNodes().forEach((node)->skillTreeNodes.add(node));
    }


    public ArrayList<SkillTreeNode> getAllNodes() {
        return skillTreeNodes;
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
        File file = new File(MMOCore.plugin.getDataFolder() + "/skillTree");
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
            register(SkillTree.loadSkillTree(YamlConfiguration.loadConfiguration(file)));
        }
    }
}
