package net.Indyuce.mmocore.manager;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigFile;
import net.Indyuce.mmocore.gui.*;
import net.Indyuce.mmocore.gui.api.EditableInventory;
import net.Indyuce.mmocore.gui.social.friend.EditableFriendList;
import net.Indyuce.mmocore.gui.social.friend.EditableFriendRemoval;
import net.Indyuce.mmocore.gui.social.guild.EditableGuildCreation;
import net.Indyuce.mmocore.gui.social.guild.EditableGuildView;
import net.Indyuce.mmocore.gui.social.party.EditablePartyCreation;
import net.Indyuce.mmocore.gui.social.party.EditablePartyView;

public class InventoryManager {
    public static final PlayerStats PLAYER_STATS = new PlayerStats();
    public static final SkillList SKILL_LIST = new SkillList();
    public static final ClassSelect CLASS_SELECT = new ClassSelect();
    public static final SubclassSelect SUBCLASS_SELECT = new SubclassSelect();
    public static final ClassConfirmation CLASS_CONFIRM = new ClassConfirmation();
    public static final SubclassConfirmation SUBCLASS_CONFIRM = new SubclassConfirmation();
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
    public static final List<EditableInventory> list = Arrays.asList(PLAYER_STATS, ATTRIBUTE_VIEW, SKILL_LIST, CLASS_SELECT, SUBCLASS_SELECT, SUBCLASS_CONFIRM, QUEST_LIST, WAYPOINTS, CLASS_CONFIRM, FRIEND_LIST, FRIEND_REMOVAL, PARTY_VIEW, PARTY_CREATION, GUILD_VIEW, GUILD_CREATION);

    public static void load() {
        list.forEach(inv -> {
            MMOCore.plugin.configManager.loadDefaultFile("gui", inv.getId() + ".yml");
            try {
                inv.reload(new ConfigFile("/gui", inv.getId()).getConfig());
            } catch (IllegalArgumentException exception) {
                MMOCore.log(Level.WARNING, "Could not load inventory " + inv.getId() + ": " + exception.getMessage());
            }
        });
    }
}
