package net.Indyuce.mmocore.gui.social.friend;

import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.input.PlayerInput.InputType;
import net.Indyuce.mmocore.api.util.math.format.DelayFormat;
import net.Indyuce.mmocore.gui.api.EditableInventory;
import net.Indyuce.mmocore.gui.api.GeneratedInventory;
import net.Indyuce.mmocore.gui.api.PluginInventory;
import net.Indyuce.mmocore.gui.api.item.InventoryItem;
import net.Indyuce.mmocore.gui.api.item.InventoryPlaceholderItem;
import net.Indyuce.mmocore.gui.api.item.NoPlaceholderItem;
import net.Indyuce.mmocore.gui.api.item.Placeholders;
import net.Indyuce.mmocore.manager.InventoryManager;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.NBTItem;

public class EditableFriendList extends EditableInventory {
	public EditableFriendList() {
		super("friend-list");
	}

	@Override
	public InventoryItem load(String function, ConfigurationSection config) {

		if (function.equals("friend"))
			return new FriendItem(config);

		if (function.equals("previous"))
			return new NoPlaceholderItem(config) {

				@Override
				public boolean canDisplay(GeneratedInventory inv) {
					return ((FriendListInventory) inv).page > 0;
				}
			};

		if (function.equals("next"))
			return new NoPlaceholderItem(config) {

				@Override
				public boolean canDisplay(GeneratedInventory inv) {
					FriendListInventory generated = (FriendListInventory) inv;
					return inv.getEditable().getByFunction("friend").getSlots().size() * generated.page < inv.getPlayerData().getFriends().size();
				}
			};

		return new NoPlaceholderItem(config);
	}

	public GeneratedInventory newInventory(PlayerData data) {
		return new FriendListInventory(data, this);
	}

	public static class OfflineFriendItem extends InventoryPlaceholderItem {
		public OfflineFriendItem(ConfigurationSection config) {
			super(config);
		}

		@Override
		public Placeholders getPlaceholders(PluginInventory inv, int n) {
			OfflinePlayer friend = Bukkit.getOfflinePlayer(inv.getPlayerData().getFriends().get(n));

			Placeholders holders = new Placeholders();
			holders.register("name", friend.getName());
			holders.register("last_seen", new DelayFormat(2).format(System.currentTimeMillis() - friend.getLastPlayed()));
			return holders;
		}

		@Override
		public ItemStack display(GeneratedInventory inv, int n) {
			OfflinePlayer friend = Bukkit.getOfflinePlayer(inv.getPlayerData().getFriends().get(n));

			ItemStack disp = super.display(inv, n);
			ItemMeta meta = disp.getItemMeta();

			if (meta instanceof SkullMeta)
				Bukkit.getScheduler().runTaskAsynchronously(MMOCore.plugin, () -> {
					((SkullMeta) meta).setOwningPlayer(friend);
					disp.setItemMeta(meta);
				});

			return NBTItem.get(disp).addTag(new ItemTag("uuid", friend.getUniqueId().toString())).toItem();
		}
	}

	public static class OnlineFriendItem extends InventoryPlaceholderItem {
		public OnlineFriendItem(ConfigurationSection config) {
			super(config);
		}

		@Override
		public Placeholders getPlaceholders(PluginInventory inv, int n) {
			Player friend = Bukkit.getPlayer(inv.getPlayerData().getFriends().get(n));
			PlayerData data = PlayerData.get(friend);

			Placeholders holders = new Placeholders();
			if(data.isOnline())
				holders.register("name", data.getPlayer().getName());
			holders.register("class", data.getProfess().getName());
			holders.register("level", data.getLevel());
			holders.register("online_since", new DelayFormat(2).format(System.currentTimeMillis() - data.getLastLogin()));
			return holders;
		}

		@Override
		public ItemStack display(GeneratedInventory inv, int n) {
			Player friend = Bukkit.getPlayer(inv.getPlayerData().getFriends().get(n));

			ItemStack disp = super.display(inv, n);
			ItemMeta meta = disp.getItemMeta();

			if (meta instanceof SkullMeta)
				Bukkit.getScheduler().runTaskAsynchronously(MMOCore.plugin, () -> {
					((SkullMeta) meta).setOwningPlayer(friend);
					disp.setItemMeta(meta);
				});

			return NBTItem.get(disp).addTag(new ItemTag("uuid", friend.getUniqueId().toString())).toItem();
		}
	}

	public static class FriendItem extends NoPlaceholderItem {
		private final OnlineFriendItem online;
		private final OfflineFriendItem offline;

		public FriendItem(ConfigurationSection config) {
			super(config);

			Validate.notNull(config.contains("online"), "Could not load online config");
			Validate.notNull(config.contains("offline"), "Could not load offline config");

			online = new OnlineFriendItem(config.getConfigurationSection("online"));
			offline = new OfflineFriendItem(config.getConfigurationSection("offline"));
		}

		@Override
		public ItemStack display(GeneratedInventory inv, int n) {
			return inv.getPlayerData().getFriends().size() <= n ? super.display(inv, n)
					: Bukkit.getOfflinePlayer(inv.getPlayerData().getFriends().get(n)).isOnline() ? online.display(inv, n) : offline.display(inv, n);
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
		public void whenClicked(InventoryClickEvent event, InventoryItem item) {
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

				long remaining = playerData.getLastFriendRequest() + 60 * 2 * 1000 - System.currentTimeMillis();
				if (remaining > 0) {
					MMOCore.plugin.configManager.getSimpleMessage("friend-request-cooldown", "cooldown", new DelayFormat().format(remaining))
							.send(player);
					return;
				}

				MMOCore.plugin.configManager.newPlayerInput(player, InputType.FRIEND_REQUEST, (input) -> {
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

			if (item.getFunction().equals("friend") && event.getAction() == InventoryAction.PICKUP_HALF)
				InventoryManager.FRIEND_REMOVAL.newInventory(playerData,
						Bukkit.getOfflinePlayer(UUID.fromString(NBTItem.get(event.getCurrentItem()).getString("uuid"))), this).open();
		}
	}

	// private int page;
	// private ItemStack next, prev, newreq;
	//
	// private int[] slots = { 10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23,
	// 24, 25, 28, 29, 30, 31, 32, 33, 34 };
	//
	// public EditableFriendList(Player player) {
	// this(player, 1);
	// }
	//
	// public EditableFriendList(Player player, int page) {
	// super(player);
	// this.page = page;
	// }
	//
	// @Override
	// public Inventory getInventory() {
	// Inventory inv = Bukkit.createInventory(this, 54, "Friends");
	//
	// int n = 0;
	// for (int j = 21 * (page - 1); j < Math.min(21 * page,
	// playerData.getFriends().size()); j++) {
	//
	// /*
	// * if the player is not found, delete it from the friend list.
	// */
	// UUID uuid = playerData.getFriends().get(j);
	// OfflinePlayer offline = Bukkit.getOfflinePlayer(uuid);
	// if (offline == null || offline.getName() == null) {
	// playerData.removeFriend(uuid);
	// continue;
	// }
	//
	// PlayerDataManager data;
	// ItemStack item = offline.isOnline() ? new
	// ConfigItem("ONLINE_FRIEND").addPlaceholders("name", offline.getName(),
	// "online_since", "" + new DelayFormat(2).format(System.currentTimeMillis()
	// - (data = PlayerDataManager.get(offline)).getLastLogin()), "class",
	// data.getProfess().getName(), "level", "" + data.getLevel()).build() : new
	// ConfigItem("OFFLINE_FRIEND").addPlaceholders("name", offline.getName(),
	// "last_seen", new DelayFormat(2).format(System.currentTimeMillis() -
	// offline.getLastPlayed())).build();
	//
	// SkullMeta meta = (SkullMeta) item.getItemMeta();
	// meta.setOwningPlayer(offline);
	// item.setItemMeta(meta);
	//
	// inv.setItem(slots[n++], NBTItem.get(item).add(new ItemTag("uuid",
	// offline.getUniqueId().toString())).toItem());
	// }
	//
	// if (page > 1)
	// inv.setItem(18, prev = new ConfigItem("PREVIOUS_PAGE").build());
	//
	// if (playerData.getFriends().size() > 21 * page)
	// inv.setItem(26, next = new ConfigItem("NEXT_PAGE").build());
	//
	// ItemStack fill = new ConfigItem("NO_FRIEND").build();
	// while (n < 21 * page)
	// inv.setItem(slots[n++], fill);
	//
	// inv.setItem(49, newreq = new ConfigItem("NEW_FRIEND_REQUEST").build());
	//
	// return inv;
	// }
}
