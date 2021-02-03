package net.Indyuce.mmocore.gui.social.guild;

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
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.NBTItem;

public class EditableGuildAdmin extends EditableInventory {
	public EditableGuildAdmin() {
		super("guild-admin");
	}

	@Override
	public InventoryItem load(String function, ConfigurationSection config) {
		return function.equals("member") ? new MemberItem(config) : new NoPlaceholderItem(config);
	}

	public static class MemberDisplayItem extends InventoryPlaceholderItem {
		public MemberDisplayItem(ConfigurationSection config) {
			super(config);
		}

		@Override
		public Placeholders getPlaceholders(PluginInventory inv, int n) {
			PlayerData member = PlayerData.get(inv.getPlayerData().getGuild().getMembers().get(n));

			Placeholders holders = new Placeholders();

			if(member.isOnline())
				holders.register("name", member.getPlayer().getName());
			holders.register("class", member.getProfess().getName());
			holders.register("level", "" + member.getLevel());
			holders.register("since", new DelayFormat(2).format(System.currentTimeMillis() - member.getLastLogin()));
			return holders;
		}

		@Override
		public ItemStack display(GeneratedInventory inv, int n) {
			PlayerData member = PlayerData.get(inv.getPlayerData().getGuild().getMembers().get(n));

			ItemStack disp = super.display(inv, n);
			ItemMeta meta = disp.getItemMeta();

			if (meta instanceof SkullMeta)
				Bukkit.getScheduler().runTaskAsynchronously(MMOCore.plugin, () -> {
					if(!member.isOnline()) return;
					((SkullMeta) meta).setOwningPlayer(member.getPlayer());
					disp.setItemMeta(meta);
				});

			return NBTItem.get(disp).addTag(new ItemTag("uuid", member.getUniqueId().toString())).toItem();
		}
	}

	public static class MemberItem extends InventoryItem {
		private final InventoryPlaceholderItem empty;
		private final MemberDisplayItem member;

		public MemberItem(ConfigurationSection config) {
			super(config);

			Validate.notNull(config.contains("empty"), "Could not load empty config");
			Validate.notNull(config.contains("member"), "Could not load member config");

			empty = new NoPlaceholderItem(config.getConfigurationSection("empty"));
			member = new MemberDisplayItem(config.getConfigurationSection("member"));
		}

		@Override
		public ItemStack display(GeneratedInventory inv, int n) {
			return inv.getPlayerData().getGuild().getMembers().count() > n ? member.display(inv, n) : empty.display(inv, n);
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
		return new GuildViewInventory(data, this);
	}

	public class GuildViewInventory extends GeneratedInventory {
		private final int max;

		public GuildViewInventory(PlayerData playerData, EditableInventory editable) {
			super(playerData, editable);

			max = editable.getByFunction("member").getSlots().size();
		}

		@Override
		public String calculateName() {
			return getName().replace("{max}", "" + max).replace("{players}", "" + getPlayerData().getGuild().getMembers().count());
		}

		@Override
		public void whenClicked(InventoryClickEvent event, InventoryItem item) {

			if (item.getFunction().equals("leave")) {
				playerData.getGuild().removeMember(playerData.getUniqueId());
				player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
				player.closeInventory();
				return;
			}

			if (item.getFunction().equals("invite")) {

				if (playerData.getGuild().getMembers().count() >= max) {
					MMOCore.plugin.configManager.getSimpleMessage("guild-is-full").send(player);
					player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
					return;
				}

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
						MMOCore.plugin.configManager.getSimpleMessage("guild-invite-cooldown", "player", target.getName(), "cooldown",
								new DelayFormat().format(remaining)).send(player);
						open();
						return;
					}

					PlayerData targetData = PlayerData.get(target);
					if (playerData.getGuild().getMembers().has(target.getUniqueId())) {
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

			if (item.getFunction().equals("member") && event.getAction() == InventoryAction.PICKUP_HALF) {
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
	}
}
