package net.Indyuce.mmocore.gui.social.friend;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerActivity;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.gui.api.InventoryClickContext;
import net.Indyuce.mmocore.api.util.input.ChatInput;
import net.Indyuce.mmocore.api.util.input.PlayerInput.InputType;
import net.Indyuce.mmocore.api.util.math.format.DelayFormat;
import net.Indyuce.mmocore.gui.api.EditableInventory;
import net.Indyuce.mmocore.gui.api.GeneratedInventory;
import net.Indyuce.mmocore.gui.api.item.InventoryItem;
import net.Indyuce.mmocore.gui.api.item.Placeholders;
import net.Indyuce.mmocore.gui.api.item.SimplePlaceholderItem;
import net.Indyuce.mmocore.manager.InventoryManager;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class EditableFriendList extends EditableInventory {
    private static final NamespacedKey UUID_NAMESPACEDKEY = new NamespacedKey(MMOCore.plugin, "Uuid");

    public EditableFriendList() {
        super("friend-list");
    }

    @Override
    public InventoryItem load(String function, ConfigurationSection config) {

        if (function.equals("friend"))
            return new FriendItem(config);

        if (function.equals("previous"))
            return new SimplePlaceholderItem<FriendListInventory>(config) {

                @Override
                public boolean canDisplay(FriendListInventory inv) {
                    return inv.page > 0;
                }
            };

        if (function.equals("next"))
            return new SimplePlaceholderItem<FriendListInventory>(config) {

                @Override
                public boolean canDisplay(FriendListInventory inv) {
                    return inv.getEditable().getByFunction("friend").getSlots().size() * inv.page < inv.getPlayerData().getFriends().size();
                }
            };

        return new SimplePlaceholderItem(config);
    }

    public GeneratedInventory newInventory(PlayerData data) {
        return new FriendListInventory(data, this);
    }

    public static class OfflineFriendItem extends InventoryItem {
        public OfflineFriendItem(FriendItem parent, ConfigurationSection config) {
            super(parent, config);
        }

        @Override
        public boolean hasDifferentDisplay() {
            return true;
        }

        @NotNull
        @Override
        public OfflinePlayer getEffectivePlayer(GeneratedInventory inv, int n) {
            return Bukkit.getOfflinePlayer(inv.getPlayerData().getFriends().get(n));
        }

        @Override
        public Placeholders getPlaceholders(GeneratedInventory inv, int n) {
            OfflinePlayer friend = getEffectivePlayer(inv, n);

            Placeholders holders = new Placeholders();
            holders.register("name", friend.getName());
            holders.register("last_seen", new DelayFormat(2).format(System.currentTimeMillis() - friend.getLastPlayed()));
            return holders;
        }
    }

    public static class OnlineFriendItem extends SimplePlaceholderItem {
        public OnlineFriendItem(FriendItem parent, ConfigurationSection config) {
            super(parent, config);
        }

        @Override
        public boolean hasDifferentDisplay() {
            return true;
        }

        @NotNull
        @Override
        public OfflinePlayer getEffectivePlayer(GeneratedInventory inv, int n) {
            return Bukkit.getOfflinePlayer(inv.getPlayerData().getFriends().get(n));
        }

        @Deprecated
        @Override
        public Placeholders getPlaceholders(GeneratedInventory inv, int n) {
            final PlayerData friendData = PlayerData.get(getEffectivePlayer(inv, n));

            Placeholders holders = new Placeholders();
            if (friendData.isOnline())
                holders.register("name", friendData.getPlayer().getName());
            holders.register("class", friendData.getProfess().getName());
            holders.register("level", friendData.getLevel());
            holders.register("online_since", new DelayFormat(2).format(System.currentTimeMillis() - friendData.getLastLogin()));
            return holders;
        }
    }

    public static class FriendItem extends SimplePlaceholderItem {
        private final OnlineFriendItem online;
        private final OfflineFriendItem offline;

        public FriendItem(ConfigurationSection config) {
            super(config);

            Validate.notNull(config.contains("online"), "Could not load online config");
            Validate.notNull(config.contains("offline"), "Could not load offline config");

            online = new OnlineFriendItem(this, config.getConfigurationSection("online"));
            offline = new OfflineFriendItem(this, config.getConfigurationSection("offline"));
        }

        @Override
        public ItemStack display(GeneratedInventory inv, int n) {
            if (inv.getPlayerData().getFriends().size() <= n)
                return super.display(inv, n);

            final OfflinePlayer friend = Bukkit.getOfflinePlayer(inv.getPlayerData().getFriends().get(n));
            ItemStack disp = (friend.isOnline() ? online : offline).display(inv, n);
            ItemMeta meta = disp.getItemMeta();
            meta.getPersistentDataContainer().set(UUID_NAMESPACEDKEY, PersistentDataType.STRING, friend.getUniqueId().toString());
            if (meta instanceof SkullMeta)
                inv.dynamicallyUpdateItem(this, n, disp, current -> {
                    ((SkullMeta) meta).setOwningPlayer(friend);
                    current.setItemMeta(meta);
                });

            disp.setItemMeta(meta);
            return disp;
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

    public class FriendListInventory extends GeneratedInventory {
        private int page;

        public FriendListInventory(PlayerData playerData, EditableInventory editable) {
            super(playerData, editable);
        }

        @Override
        public String calculateName() {
            return getName();
        }

        @Override
        public void whenClicked(InventoryClickContext context, InventoryItem item) {
            if (item.getFunction().equals("previous")) {
                page--;
                open();
                return;
            }

            if (item.getFunction().equals("next")) {
                page++;
                open();
                return;
            }

            if (item.getFunction().equals("request")) {

                long remaining = playerData.getActivityTimeOut(PlayerActivity.FRIEND_REQUEST);
                if (remaining > 0) {
                    MMOCore.plugin.configManager.getSimpleMessage("friend-request-cooldown", "cooldown", new DelayFormat().format(remaining))
                            .send(player);
                    return;
                }

                new ChatInput(player, InputType.FRIEND_REQUEST, context.getInventoryHolder(), input -> {
                    Player target = Bukkit.getPlayer(input);
                    if (target == null) {
                        MMOCore.plugin.configManager.getSimpleMessage("not-online-player", "player", input).send(player);
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                        open();
                        return;
                    }

                    if (playerData.hasFriend(target.getUniqueId())) {
                        MMOCore.plugin.configManager.getSimpleMessage("already-friends", "player", target.getName()).send(player);
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                        open();
                        return;
                    }

                    if (playerData.getUniqueId().equals(target.getUniqueId())) {
                        MMOCore.plugin.configManager.getSimpleMessage("cant-request-to-yourself").send(player);
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                        open();
                        return;
                    }

                    playerData.sendFriendRequest(PlayerData.get(target));
                    MMOCore.plugin.configManager.getSimpleMessage("sent-friend-request", "player", target.getName()).send(player);
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                    open();
                });
            }

            if (item.getFunction().equals("friend") && context.getClickType() == ClickType.RIGHT) {
                String tag = context.getClickedItem().getItemMeta().getPersistentDataContainer().get(UUID_NAMESPACEDKEY, PersistentDataType.STRING);
                if (tag == null || tag.isEmpty())
                    return;

                InventoryManager.FRIEND_REMOVAL.newInventory(playerData, Bukkit.getOfflinePlayer(UUID.fromString(tag)), this).open();
            }
        }
    }
}
