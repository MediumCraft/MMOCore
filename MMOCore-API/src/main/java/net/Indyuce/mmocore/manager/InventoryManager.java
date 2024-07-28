package net.Indyuce.mmocore.manager;

import io.lumine.mythic.lib.UtilityMethods;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigFile;
import net.Indyuce.mmocore.gui.*;
import net.Indyuce.mmocore.gui.api.EditableInventory;
import net.Indyuce.mmocore.gui.skilltree.SkillTreeViewer;
import net.Indyuce.mmocore.gui.social.friend.EditableFriendList;
import net.Indyuce.mmocore.gui.social.friend.EditableFriendRemoval;
import net.Indyuce.mmocore.gui.social.guild.EditableGuildCreation;
import net.Indyuce.mmocore.gui.social.guild.EditableGuildView;
import net.Indyuce.mmocore.gui.social.party.EditablePartyCreation;
import net.Indyuce.mmocore.gui.social.party.EditablePartyView;

import java.util.*;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class InventoryManager {
    public static final PlayerStats PLAYER_STATS = new PlayerStats();
    public static final SkillList SKILL_LIST = new SkillList();
    public static final ClassSelect CLASS_SELECT = new ClassSelect();
    public static final SubclassSelect SUBCLASS_SELECT = new SubclassSelect();
    public static final WaypointViewer WAYPOINTS = new WaypointViewer();
    public static final EditableFriendList FRIEND_LIST = new EditableFriendList();
    public static final EditableFriendRemoval FRIEND_REMOVAL = new EditableFriendRemoval();
    public static final EditablePartyView PARTY_VIEW = new EditablePartyView();
    public static final EditablePartyCreation PARTY_CREATION = new EditablePartyCreation();
    public static final EditableGuildView GUILD_VIEW = new EditableGuildView();
    public static final EditableGuildCreation GUILD_CREATION = new EditableGuildCreation();
    public static final QuestViewer QUEST_LIST = new QuestViewer();
    public static final AttributeView ATTRIBUTE_VIEW = new AttributeView();
    public static final SkillTreeViewer TREE_VIEW = new SkillTreeViewer();
    public static Map<String, SkillTreeViewer> SPECIFIC_TREE_VIEW = new HashMap<>();
    public static final Map<String, ClassConfirmation> CLASS_CONFIRM = new HashMap<>();


    public static final List<EditableInventory> list = new ArrayList(Arrays.asList(PLAYER_STATS, ATTRIBUTE_VIEW, TREE_VIEW, SKILL_LIST, CLASS_SELECT, SUBCLASS_SELECT, QUEST_LIST, WAYPOINTS, FRIEND_LIST, FRIEND_REMOVAL, PARTY_VIEW, PARTY_CREATION, GUILD_VIEW, GUILD_CREATION));

    public static void load() {
        //Loads the specific inventories
        for (SpecificInventoryLoader loader : SpecificInventoryLoader.values()) {
            try {
                MMOCore.plugin.configManager.copyDefaultFile("gui/" + loader.name + "/" + loader.name + "-default.yml");
            } catch (Exception exception) {
                MMOCore.log(Level.WARNING, "Could not load inventory 'gui/" + loader.name + "/" + loader.name + "-default" + "': " + exception.getMessage());
            }
            for (String id : loader.ids) {
                String formattedId = UtilityMethods.ymlName(id);
                final ConfigFile configFile = new ConfigFile("/gui/" + loader.name, loader.name + "-" + formattedId);
                final EditableInventory GUI = loader.provider.apply(id, !configFile.exists());
                loader.inventories.put(formattedId, GUI);
                GUI.reload(new ConfigFile("/gui/" + loader.name, GUI.getId()).getConfig());
            }
        }
        list.forEach(inv ->
        {
            try {
                MMOCore.plugin.configManager.copyDefaultFile("gui/" + inv.getId() + ".yml");
                inv.reload(new ConfigFile("/gui", inv.getId()).getConfig());
            } catch (Exception exception) {
                MMOCore.log(Level.WARNING, "Could not load inventory '" + (inv instanceof ClassConfirmation ? "class-confirm/" : "") + inv.getId() + "': " + exception.getMessage());
            }
        });
    }

    public enum SpecificInventoryLoader {
        CLASS_CONFIRM("class-confirm",
                InventoryManager.CLASS_CONFIRM,
                MMOCore.plugin.classManager.getAll().
                        stream().
                        map(playerClass -> playerClass.getId()).
                        collect(Collectors.toList()),
                (id, isDefault) -> new ClassConfirmation(MMOCore.plugin.classManager.get(id), isDefault)
        ),

        SPECIFIC_TREE("specific-skill-tree",
                InventoryManager.SPECIFIC_TREE_VIEW,
                MMOCore.plugin.skillTreeManager.getAll().
                        stream().
                        map(skillTree -> skillTree.getId()).
                        collect(Collectors.toList()),
                (id, isDefault) -> new SkillTreeViewer(MMOCore.plugin.skillTreeManager.get(id), isDefault));

        private final String name;

        private final Map inventories;

        private final List<String> ids;

        private final BiFunction<String, Boolean, ? extends EditableInventory> provider;

        SpecificInventoryLoader(String name, Map inventories, List<String> ids,
                                BiFunction<String, Boolean, ? extends EditableInventory> provider) {
            this.name = name;
            this.inventories = inventories;
            this.ids = ids;
            this.provider = provider;
        }

    }
}
