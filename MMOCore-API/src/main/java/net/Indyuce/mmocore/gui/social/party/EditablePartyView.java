package net.Indyuce.mmocore.gui.social.party;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.format.DelayFormat;
import net.Indyuce.mmocore.gui.api.InventoryClickContext;
import net.Indyuce.mmocore.party.provided.Party;
import net.Indyuce.mmocore.api.util.input.ChatInput;
import net.Indyuce.mmocore.api.util.input.PlayerInput;
import net.Indyuce.mmocore.gui.api.GeneratedInventory;
import net.Indyuce.mmocore.gui.api.item.InventoryItem;
import net.Indyuce.mmocore.gui.api.item.SimplePlaceholderItem;
import net.Indyuce.mmocore.gui.api.EditableInventory;
import net.Indyuce.mmocore.gui.api.item.Placeholders;
import org.apache.commons.lang.Validate;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class EditablePartyView extends EditableInventory {
    private static final NamespacedKey UUID_NAMESPACEDKEY = new NamespacedKey(MMOCore.plugin, "Uuid");

    public EditablePartyView() {
        super("party-view");
    }

    @Override
    public InventoryItem load(String function, ConfigurationSection config) {
        return function.equals("member") ? new MemberItem(config) : new SimplePlaceholderItem(config);
    }

    public static class MemberDisplayItem extends InventoryItem {
        public MemberDisplayItem(MemberItem memberItem, ConfigurationSection config) {
            super(memberItem, config);
        }

        @Override
        public boolean hasDifferentDisplay() {
            return true;
        }

        @Override
        public Placeholders getPlaceholders(GeneratedInventory inv, int n) {
            Party party = (Party) inv.getPlayerData().getParty();
            PlayerData member = party.getMembers().get(n);

            Placeholders holders = new Placeholders();
            if (member.isOnline())
                holders.register("name", member.getPlayer().getName());
            holders.register("class", member.getProfess().getName());
            holders.register("level", "" + member.getLevel());
            holders.register("since", new DelayFormat(2).format(System.currentTimeMillis() - member.getLastLogin()));
            return holders;
        }

        @Override
        public ItemStack display(GeneratedInventory inv, int n) {
            Party party = (Party) inv.getPlayerData().getParty();
            PlayerData member = party.getMembers().get(n);

            ItemStack disp = super.display(inv, n);
            ItemMeta meta = disp.getItemMeta();
            meta.getPersistentDataContainer().set(UUID_NAMESPACEDKEY, PersistentDataType.STRING, member.getUniqueId().toString());

            if (meta instanceof SkullMeta)
                inv.dynamicallyUpdateItem(this, n, disp, current -> {
                    ((SkullMeta) meta).setOwningPlayer(member.getPlayer());
                    current.setItemMeta(meta);
                });

            disp.setItemMeta(meta);
            return disp;
        }
    }

    public static class MemberItem extends SimplePlaceholderItem {
        private final InventoryItem empty;
        private final MemberDisplayItem member;

        public MemberItem(ConfigurationSection config) {
            super(Material.BARRIER, config);

            Validate.notNull(config.contains("empty"), "Could not load empty config");
            Validate.notNull(config.contains("member"), "Could not load member config");

            empty = new SimplePlaceholderItem(config.getConfigurationSection("empty"));
            member = new MemberDisplayItem(this, config.getConfigurationSection("member"));
        }

        @Override
        public ItemStack display(GeneratedInventory inv, int n) {
            Party party = (Party) inv.getPlayerData().getParty();
            return party.getMembers().size() > n ? member.display(inv, n) : empty.display(inv, n);
        }

        @Override
        public boolean hasDifferentDisplay() {
            return true;
        }

        @Override
        public boolean canDisplay(GeneratedInventory inv) {
            return true;
        }
    }

    public GeneratedInventory newInventory(PlayerData data) {
        return new PartyViewInventory(data, this);
    }

    public class PartyViewInventory extends GeneratedInventory {
        private final int max;

        public PartyViewInventory(PlayerData playerData, EditableInventory editable) {
            super(playerData, editable);

            max = editable.getByFunction("member").getSlots().size();
        }

        @Override
        public String calculateName() {
            Party party = (Party) getPlayerData().getParty();
            return getName().replace("{max}", "" + max).replace("{players}", "" + party.getMembers().size());
        }

        @Override
        public void whenClicked(InventoryClickContext context, InventoryItem item) {
            Party party = (Party) playerData.getParty();

            if (item.getFunction().equals("leave")) {
                party.removeMember(playerData);
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                player.closeInventory();
                return;
            }

            if (item.getFunction().equals("invite")) {

                if (party.getMembers().size() >= max) {
                    MMOCore.plugin.configManager.getSimpleMessage("party-is-full").send(player);
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                    return;
                }

                new ChatInput(player, PlayerInput.InputType.PARTY_INVITE, context.getInventoryHolder(), input -> {
                    Player target = Bukkit.getPlayer(input);
                    if (target == null) {
                        MMOCore.plugin.configManager.getSimpleMessage("not-online-player", "player", input).send(player);
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                        open();
                        return;
                    }

                    long remaining = party.getLastInvite(target) + 60 * 2 * 1000 - System.currentTimeMillis();
                    if (remaining > 0) {
                        MMOCore.plugin.configManager.getSimpleMessage("party-invite-cooldown", "player", target.getName(), "cooldown", new DelayFormat().format(remaining)).send(player);
                        open();
                        return;
                    }

                    PlayerData targetData = PlayerData.get(target);
                    if (party.hasMember(target)) {
                        MMOCore.plugin.configManager.getSimpleMessage("already-in-party", "player", target.getName()).send(player);
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                        open();
                        return;
                    }

                    int levelDifference = Math.abs(targetData.getLevel() - party.getLevel());
                    if (levelDifference > MMOCore.plugin.configManager.maxPartyLevelDifference) {
                        MMOCore.plugin.configManager.getSimpleMessage("high-level-difference", "player", target.getName(), "diff", String.valueOf(levelDifference)).send(player);
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                        open();
                        return;
                    }

                    party.sendInvite(playerData, targetData);
                    MMOCore.plugin.configManager.getSimpleMessage("sent-party-invite", "player", target.getName()).send(player);
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                    open();
                });
            }

            if (item.getFunction().equals("member") && context.getClickType() == ClickType.RIGHT) {
                if (!party.getOwner().equals(playerData))
                    return;

                OfflinePlayer target = Bukkit.getOfflinePlayer(UUID.fromString(context.getClickedItem().getItemMeta().getPersistentDataContainer().get(UUID_NAMESPACEDKEY, PersistentDataType.STRING)));
                if (target.equals(player))
                    return;

                party.removeMember(PlayerData.get(target));
                MMOCore.plugin.configManager.getSimpleMessage("kick-from-party", "player", target.getName()).send(player);
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
            }
        }
    }
}
