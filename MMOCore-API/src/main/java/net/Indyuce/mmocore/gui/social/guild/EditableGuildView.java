package net.Indyuce.mmocore.gui.social.guild;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.format.DelayFormat;
import net.Indyuce.mmocore.gui.api.InventoryClickContext;
import net.Indyuce.mmocore.api.util.input.ChatInput;
import net.Indyuce.mmocore.api.util.input.PlayerInput;
import net.Indyuce.mmocore.gui.api.GeneratedInventory;
import net.Indyuce.mmocore.gui.api.item.InventoryItem;
import net.Indyuce.mmocore.gui.api.item.SimplePlaceholderItem;
import net.Indyuce.mmocore.api.player.OfflinePlayerData;
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

import java.util.List;
import java.util.UUID;

public class EditableGuildView extends EditableInventory {
	private static final NamespacedKey UUID_NAMESPACEDKEY = new NamespacedKey(MMOCore.plugin, "Uuid");

	public EditableGuildView() {
		super("guild-view");
	}

	@Override
	public InventoryItem load(String function, ConfigurationSection config) {
		return function.equals("member") ? new MemberItem(config) : (function.equals("next") || function.equals("previous") || function.equals("disband") || function.equals("invite")) ? new ConditionalItem(function, config) : new SimplePlaceholderItem(config);
	}

	public static class MemberDisplayItem extends InventoryItem<GuildViewInventory> {
		public MemberDisplayItem(MemberItem memberItem, ConfigurationSection config) {
			super(memberItem, config);
		}

		@Override
		public boolean hasDifferentDisplay() {
			return true;
		}

		@Override
		public Placeholders getPlaceholders(GuildViewInventory inv, int n) {
			UUID uuid = inv.members.get(n);
			Placeholders holders = new Placeholders();
			/*
			 * Will never be null since a players name will always be recorded
			 * if they've been in a guild
			 */
			holders.register("name", Bukkit.getOfflinePlayer(uuid).getName());

			OfflinePlayerData offline = OfflinePlayerData.get(uuid);
			holders.register("class", offline.getProfess().getName());
			holders.register("level", offline.getLevel());
			holders.register("since", new DelayFormat(2).format(System.currentTimeMillis() - offline.getLastLogin()));

			return holders;
		}

		@Override
		public ItemStack display(GuildViewInventory inv, int n) {
			UUID uuid = inv.members.get(n);

			ItemStack disp = super.display(inv, n);
			ItemMeta meta = disp.getItemMeta();
			meta.getPersistentDataContainer().set(UUID_NAMESPACEDKEY, PersistentDataType.STRING, uuid.toString());

			if (meta instanceof SkullMeta)
				inv.dynamicallyUpdateItem(this, n, disp, current -> {
					((SkullMeta) meta).setOwningPlayer(Bukkit.getOfflinePlayer(uuid));
					current.setItemMeta(meta);
				});

			disp.setItemMeta(meta);
			return disp;
		}
	}

	public class MemberItem extends SimplePlaceholderItem<GuildViewInventory> {
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
		public ItemStack display(GuildViewInventory inv, int n) {
			int index = n * inv.getPage();
			return inv.getPlayerData().getGuild().countMembers() > index ? member.display(inv, index) : empty.display(inv, index);
		}

		@Override
		public boolean hasDifferentDisplay() {
			return true;
		}
	}

	public class ConditionalItem extends SimplePlaceholderItem<GuildViewInventory> {
		private final String function;

		public ConditionalItem(String func, ConfigurationSection config) {
			super(config);
			this.function = func;
		}

		@Override
		public ItemStack display(GuildViewInventory inv, int n) {

			if (function.equals("next"))
				if (inv.getPage() == (inv.getPlayerData().getGuild().countMembers() + 20)
						/ inv.getByFunction("member").getSlots().size())
					return null;
			if (function.equals("previous") && inv.getPage() == 1)
				return null;
			if ((function.equals("disband") || function.equals("invite")) && !inv.getPlayerData().getGuild().getOwner().equals(inv.getPlayer().getUniqueId()))
				return null;
			return super.display(inv, n);
		}
	}

	public GeneratedInventory newInventory(PlayerData data) {
		return new GuildViewInventory(data, this);
	}

	public class GuildViewInventory extends GeneratedInventory {
		private final int maxpages;

		private int page = 1;
		private List<UUID> members;

		public GuildViewInventory(PlayerData playerData, EditableInventory editable) {
			super(playerData, editable);

			maxpages = (playerData.getGuild().countMembers() + 20) / editable.getByFunction("member").getSlots().size();
		}

		@Override
		public void open() {
			members = playerData.getGuild().listMembers();
			super.open();
		}

		@Override
		public String calculateName() {
			return getName().replace("{online_players}", "" + getPlayerData().getGuild().countOnlineMembers()).replace("{page}", "" + page).replace("{maxpages}", "" + maxpages).replace("{players}", String.valueOf(getPlayerData().getGuild().countMembers())).replace("{tag}", getPlayerData().getGuild().getTag()).replace("{name}", getPlayerData().getGuild().getName());
		}

		@Override
		public void whenClicked(InventoryClickContext context, InventoryItem item) {
			if (item.getFunction().equals("leave")) {
				playerData.getGuild().removeMember(playerData.getUniqueId());
				player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
				player.closeInventory();
				return;
			}

			if (item.getFunction().equals("next") && page != maxpages) {
				page++;
				open();
				return;
			}

			if (item.getFunction().equals("previous") && page != 1) {
				page--;
				open();
				return;
			}

			if (item.getFunction().equals("disband")) {
				if (!playerData.getGuild().getOwner().equals(playerData.getUniqueId()))
					return;
				MMOCore.plugin.dataProvider.getGuildManager().unregisterGuild(playerData.getGuild());
				player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
				player.closeInventory();
				return;
			}

			if (item.getFunction().equals("invite")) {
				if (!playerData.getGuild().getOwner().equals(playerData.getUniqueId()))
					return;

				/*
				 * if (playerData.getGuild().getMembers().count() >= max) {
				 * MMOCore.plugin.configManager.getSimpleMessage("guild-is-full").send(player);
				 * player.playSound(player.getLocation(),
				 * Sound.ENTITY_VILLAGER_NO, 1, 1); return; }
				 */

				new ChatInput(player, PlayerInput.InputType.GUILD_INVITE, context.getInventoryHolder(), input -> {
					Player target = Bukkit.getPlayer(input);
					if (target == null) {
						MMOCore.plugin.configManager.getSimpleMessage("not-online-player", "player", input).send(player);
						player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
						open();
						return;
					}

					long remaining = playerData.getGuild().getLastInvite(target) + 60 * 2 * 1000 - System.currentTimeMillis();
					if (remaining > 0) {
						MMOCore.plugin.configManager.getSimpleMessage("guild-invite-cooldown", "player", target.getName(), "cooldown", new DelayFormat().format(remaining)).send(player);
						open();
						return;
					}

					PlayerData targetData = PlayerData.get(target);
					if (playerData.getGuild().hasMember(targetData.getUniqueId())) {
						MMOCore.plugin.configManager.getSimpleMessage("already-in-guild", "player", target.getName()).send(player);
						player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
						open();
						return;
					}

					playerData.getGuild().sendGuildInvite(playerData, targetData);
					MMOCore.plugin.configManager.getSimpleMessage("sent-guild-invite", "player", target.getName()).send(player);
					player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
					open();
				});
			}

			if (item.getFunction().equals("member") && context.getClickType() == ClickType.RIGHT) {
				if (!playerData.getGuild().getOwner().equals(playerData.getUniqueId()))
					return;

				String tag = context.getClickedItem().getItemMeta().getPersistentDataContainer().get(UUID_NAMESPACEDKEY, PersistentDataType.STRING);
				if (tag == null || tag.isEmpty())
					return;

				OfflinePlayer target = Bukkit.getOfflinePlayer(UUID.fromString(tag));
				if (target.equals(player))
					return;

				playerData.getGuild().removeMember(target.getUniqueId());
				MMOCore.plugin.configManager.getSimpleMessage("kick-from-guild", "player", target.getName()).send(player);
				player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
			}
		}

		public int getPage() {
			return page;
		}
	}
}
