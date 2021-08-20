package net.Indyuce.mmocore.gui.social.party;

import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.input.PlayerInput.InputType;
import net.Indyuce.mmocore.api.util.math.format.DelayFormat;
import net.Indyuce.mmocore.gui.api.EditableInventory;
import net.Indyuce.mmocore.gui.api.GeneratedInventory;
import net.Indyuce.mmocore.gui.api.PluginInventory;
import net.Indyuce.mmocore.gui.api.item.InventoryItem;
import net.Indyuce.mmocore.gui.api.item.Placeholders;
import net.Indyuce.mmocore.gui.api.item.SimplePlaceholderItem;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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


public class EditablePartyView extends EditableInventory {
	public EditablePartyView() {
		super("party-view");
	}

	@Override
	public InventoryItem load(String function, ConfigurationSection config) {
		return function.equals("member") ? new MemberItem(config) : new SimplePlaceholderItem(config);
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
			PlayerData member = inv.getPlayerData().getParty().getMembers().get(n);

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
			PlayerData member = inv.getPlayerData().getParty().getMembers().get(n);

			ItemStack disp = super.display(inv, n);
			ItemMeta meta = disp.getItemMeta();

			/*
			 * run async to save performance
			 */
			if (meta instanceof SkullMeta)
				Bukkit.getScheduler().runTaskAsynchronously(MMOCore.plugin, () -> {
					if (!member.isOnline()) return;
					((SkullMeta) meta).setOwningPlayer(member.getPlayer());
					disp.setItemMeta(meta);
				});

			return NBTItem.get(disp).addTag(new ItemTag("uuid", member.getUniqueId().toString())).toItem();
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
			member = new MemberDisplayItem(config.getConfigurationSection("member"));
		}

		@Override
		public ItemStack display(GeneratedInventory inv, int n) {
			return inv.getPlayerData().getParty().getMembers().count() > n ? member.display(inv, n) : empty.display(inv, n);
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
			return getName().replace("{max}", "" + max).replace("{players}", "" + getPlayerData().getParty().getMembers().count());
		}

		@Override
		public void whenClicked(InventoryClickEvent event, InventoryItem item) {

			if (item.getFunction().equals("leave")) {
				playerData.getParty().removeMember(playerData);
				player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
				player.closeInventory();
				return;
			}

			if (item.getFunction().equals("invite")) {

				if (playerData.getParty().getMembers().count() >= max) {
					MMOCore.plugin.configManager.getSimpleMessage("party-is-full").send(player);
					player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
					return;
				}

				MMOCore.plugin.configManager.newPlayerInput(player, InputType.PARTY_INVITE, (input) -> {
					Player target = Bukkit.getPlayer(input);
					if (target == null) {
						MMOCore.plugin.configManager.getSimpleMessage("not-online-player", "player", input).send(player);
						player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
						open();
						return;
					}

					long remaining = playerData.getParty().getLastInvite(target) + 60 * 2 * 1000 - System.currentTimeMillis();
					if (remaining > 0) {
						MMOCore.plugin.configManager.getSimpleMessage("party-invite-cooldown", "player", target.getName(), "cooldown", new DelayFormat().format(remaining)).send(player);
						open();
						return;
					}

					PlayerData targetData = PlayerData.get(target);
					if (playerData.getParty().getMembers().has(targetData)) {
						MMOCore.plugin.configManager.getSimpleMessage("already-in-party", "player", target.getName()).send(player);
						player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
						open();
						return;
					}

					playerData.getParty().sendPartyInvite(playerData, targetData);
					MMOCore.plugin.configManager.getSimpleMessage("sent-party-invite", "player", target.getName()).send(player);
					player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
					open();
				});
			}

			if (item.getFunction().equals("member") && event.getAction() == InventoryAction.PICKUP_HALF) {
				if (!playerData.getParty().getOwner().equals(playerData))
					return;

				OfflinePlayer target = Bukkit.getOfflinePlayer(UUID.fromString(NBTItem.get(event.getCurrentItem()).getString("uuid")));
				if (target.equals(player))
					return;

				playerData.getParty().removeMember(PlayerData.get(target));
				MMOCore.plugin.configManager.getSimpleMessage("kick-from-party", "player", target.getName()).send(player);
				player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
			}
		}
	}
}
