package net.Indyuce.mmocore.manager;

import java.util.*;
import java.util.logging.Level;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.profess.PlayerClass;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import net.Indyuce.mmocore.gui.skilltree.SkillTreeViewer;
import net.Indyuce.mmocore.gui.social.friend.EditableFriendList;
import net.Indyuce.mmocore.gui.*;
import net.Indyuce.mmocore.gui.social.friend.EditableFriendRemoval;
import net.Indyuce.mmocore.gui.social.guild.EditableGuildCreation;
import net.Indyuce.mmocore.gui.social.guild.EditableGuildView;
import net.Indyuce.mmocore.gui.social.party.EditablePartyCreation;
import net.Indyuce.mmocore.gui.social.party.EditablePartyView;
import net.Indyuce.mmocore.api.ConfigFile;
import net.Indyuce.mmocore.gui.api.EditableInventory;

public class InventoryManager {
    public static final PlayerStats PLAYER_STATS = new PlayerStats();
    public static final SkillList SKILL_LIST = new SkillList();
    public static final ClassSelect CLASS_SELECT = new ClassSelect();
    public static final SubclassSelect SUBCLASS_SELECT = new SubclassSelect();
    public static final Map<String, ClassConfirmation> CLASS_CONFIRM = new HashMap<>();
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

    public static final List<EditableInventory> list = new ArrayList(Arrays.asList(PLAYER_STATS, ATTRIBUTE_VIEW, TREE_VIEW, SKILL_LIST, CLASS_SELECT, SUBCLASS_SELECT, QUEST_LIST, WAYPOINTS, FRIEND_LIST, FRIEND_REMOVAL, PARTY_VIEW, PARTY_CREATION, GUILD_VIEW, GUILD_CREATION));

    public static void load() {
        final String classConfirmFolder = "gui/class-confirm";
        try {
            MMOCore.plugin.configManager.loadDefaultFile(classConfirmFolder, "class-confirm-default.yml");
        } catch (Exception exception) {
            MMOCore.log(Level.WARNING, "Could not load inventory 'class-confirm/class-confirm-default" + "': " + exception.getMessage());
        }

        for (PlayerClass playerClass : MMOCore.plugin.classManager.getAll()) {
            final String classId = MMOCoreUtils.ymlName(playerClass.getId());
            final ConfigFile configFile = new ConfigFile(classConfirmFolder, "class-confirm-" + classId);
            final ClassConfirmation GUI = configFile.exists() ? new ClassConfirmation(playerClass, false) : new ClassConfirmation(playerClass, true);
            CLASS_CONFIRM.put(MMOCoreUtils.ymlName(playerClass.getId()), GUI);
            GUI.reload(new ConfigFile("/" + classConfirmFolder, GUI.getId()).getConfig());
        }

        list.forEach(inv ->
        {
            String folder = "gui" + (inv instanceof ClassConfirmation ? "/class-confirm" : "");
            try {
                MMOCore.plugin.configManager.loadDefaultFile(folder, inv.getId() + ".yml");
                inv.reload(new ConfigFile("/" + folder, inv.getId()).getConfig());
            } catch (Exception exception) {
                MMOCore.log(Level.WARNING, "Could not load inventory '" + (inv instanceof ClassConfirmation ? "class-confirm/" : "") + inv.getId() + "': " + exception.getMessage());
            }
        });
    }
}
