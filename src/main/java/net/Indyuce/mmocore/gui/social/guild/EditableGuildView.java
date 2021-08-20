package net.Indyuce.mmocore.gui.social.guild;

import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.OfflinePlayerData;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.input.PlayerInput.InputType;
import net.Indyuce.mmocore.api.util.math.format.DelayFormat;
import net.Indyuce.mmocore.gui.api.EditableInventory;
import net.Indyuce.mmocore.gui.api.GeneratedInventory;
import net.Indyuce.mmocore.gui.api.item.InventoryItem;
import net.Indyuce.mmocore.gui.api.item.Placeholders;
import net.Indyuce.mmocore.gui.api.item.SimplePlaceholderItem;
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

import java.util.UUID;

public class EditableGuildView extends EditableInventory {
	public EditableGuildView() {
		super("guild-view");
	}

	@Override
	public InventoryItem load(String function, ConfigurationSection config) {
		return function.equals("member") ? new MemberItem(config) : (function.equals("next") || function.equals("previous") || function.equals("disband") || function.equals("invite")) ? new ConditionalItem(function, config) : new SimplePlaceholderItem(config);
	}

	public static class MemberDisplayItem extends InventoryItem {
		public MemberDisplayItem(ConfigurationSection config) {
			super(config);
		}

		@Override
		public boolean hasDifferentDisplay() {
			return true;
		}

		@Override
		public Placeholders getPlaceholders(GeneratedInventory inv, int n) {
			UUID uuid = inv.getPlayerData().getGuild().getMembers().get(n);
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
		public ItemStack display(GeneratedInventory inv, int n) {
			UUID uuid = inv.getPlayerData().getGuild().getMembers().get(n);

			ItemStack disp = super.display(inv, n);
			ItemMeta meta = disp.getItemMeta();

			if (meta instanceof SkullMeta)
				Bukkit.getScheduler().runTaskAsynchronously(MMOCore.plugin, () -> {
					((SkullMeta) meta).setOwningPlayer(Bukkit.getOfflinePlayer(uuid));
					disp.setItemMeta(meta);
				});

			return NBTItem.get(disp).addTag(new ItemTag("uuid", uuid.toString())).toItem();
		}
	}

	public class MemberItem extends SimplePlaceholderItem<GuildViewInventory> {
		private final InventoryItem empty;
		private final MemberDisplayItem member;

		public MemberItem(ConfigurationSection config) {
			super(config);

			Validate.notNull(config.contains("empty"), "Could not load empty config");
			Validate.notNull(config.contains("member"), "Could not load member config");

			empty = new SimplePlaceholderItem(config.getConfigurationSection("empty"));
			member = new MemberDisplayItem(config.getConfigurationSection("member"));
		}

		@Override
		public ItemStack display(GuildViewInventory inv, int n) {
			int index = n * inv.getPage();
			return inv.getPlayerData().getGuild().getMembers().count() > index ? member.display(inv, index) : empty.display(inv, index);
		}

		@Override
		public boolean hasDifferentDisplay() {
			return true;
		}
	}

	public class ConditionalItem extends SimplePlaceholderItem {
		private final String function;

		public ConditionalItem(String func, ConfigurationSection config) {
			super(config);
			this.function = func;
		}

		@Override
		public ItemStack display(GeneratedInventory invpar, int n) {
			GuildViewInventory inv = (GuildViewInventory) invpar;

			if (function.equals("next"))
				if (inv.getPage() == (inv.getPlayerData().getGuild().getMembers().count() + 20)
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
		private int page = 1;
		private final int maxpages;

		public GuildViewInventory(PlayerData playerData, EditableInventory editable) {
			super(playerData, editable);

			maxpages = (playerData.getGuild().getMembers().count() + 20)
					/ editable.getByFunction("member").getSlots().size();
		}

		@Override
		public String calculateName() {
			return getName().replace("{online_players}", "" + getPlayerData().getGuild().getMembers().countOnline()).replace("{page}", "" + page).replace("{maxpages}", "" + maxpages).replace("{players}", "" + getPlayerData().getGuild().getMembers().count()).replace("{tag}", getPlayerData().getGuild().getTag()).replace("{name}", getPlayerData().getGuild().getName());
		}

		@Override
		public void whenClicked(InventoryClickEvent event, InventoryItem item) {
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

				MMOCore.plugin.configManager.newPlayerInput(player, InputType.GUILD_INVITE, (input) -> {
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
					if (playerData.getGuild().getMembers().has(targetData.getUniqueId())) {
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

			if (item.getFunction().equals("member") && event.getAction() == InventoryAction.PICKUP_HALF && !NBTItem.get(event.getCurrentItem()).getString("uuid").isEmpty()) {
				if (!playerData.getGuild().getOwner().equals(playerData.getUniqueId()))
					return;

				OfflinePlayer target = Bukkit.getOfflinePlayer(UUID.fromString(NBTItem.get(event.getCurrentItem()).getString("uuid")));
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
