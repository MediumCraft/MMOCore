package net.Indyuce.mmocore.gui.social.guild;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.input.PlayerInput.InputType;
import net.Indyuce.mmocore.gui.api.EditableInventory;
import net.Indyuce.mmocore.gui.api.GeneratedInventory;
import net.Indyuce.mmocore.gui.api.item.InventoryItem;
import net.Indyuce.mmocore.gui.api.item.SimplePlaceholderItem;
import net.Indyuce.mmocore.manager.InventoryManager;
import net.Indyuce.mmocore.manager.data.GuildDataManager.GuildConfiguration.NamingRules;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class EditableGuildCreation extends EditableInventory {
	public EditableGuildCreation() {
		super("guild-creation");
	}

	@Override
	public InventoryItem load(String function, ConfigurationSection config) {
        return new SimplePlaceholderItem(config);
    }

	public GeneratedInventory newInventory(PlayerData data) {
		return new GuildCreationInventory(data, this);
	}

	public class GuildCreationInventory extends GeneratedInventory {
		public GuildCreationInventory(PlayerData playerData, EditableInventory editable) {
			super(playerData, editable);
		}

		@Override
		public void whenClicked(InventoryClickEvent event, InventoryItem item) {
			if (event.getInventory() != event.getClickedInventory())
				return;

			if (item.getFunction().equals("create")) {
				MMOCore.plugin.configManager.newPlayerInput(player, InputType.GUILD_CREATION_TAG, (input) -> {
					if(MMOCore.plugin.dataProvider.getGuildManager().getConfig().shouldUppercaseTags())
						input = input.toUpperCase();
						
					if(check(player, input, MMOCore.plugin.dataProvider.getGuildManager().getConfig().getTagRules())) {
						String tag = input;
						
						MMOCore.plugin.configManager.newPlayerInput(player, InputType.GUILD_CREATION_NAME, (name) -> {								
							if(check(player, name, MMOCore.plugin.dataProvider.getGuildManager().getConfig().getNameRules())) {
								MMOCore.plugin.dataProvider.getGuildManager().newRegisteredGuild(playerData.getUniqueId(), name, tag);
								MMOCore.plugin.dataProvider.getGuildManager().getGuild(tag.toLowerCase()).addMember(playerData.getUniqueId());

								InventoryManager.GUILD_VIEW.newInventory(playerData).open();
								player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
							}
						});
					}
				});
				
				return;
			}

			if (item.getFunction().equals("back"))
				player.closeInventory();
		}

		@Override
		public String calculateName() {
			return getName();
		}
	}

	public boolean check(Player player, String input, NamingRules rules) {
		String reason;
		
		if(input.length() <= rules.getMax() && input.length() >= rules.getMin())
			if(input.matches(rules.getRegex()))
				if(!MMOCore.plugin.dataProvider.getGuildManager().isRegistered(input))
					return true;
				else
					reason = MMOCore.plugin.configManager.getSimpleMessage("guild-creation.reasons.already-taken").message();
			else
				reason = MMOCore.plugin.configManager.getSimpleMessage("guild-creation.reasons.invalid-characters").message();
		else
			reason = MMOCore.plugin.configManager.getSimpleMessage("guild-creation.reasons.invalid-length", "min", "" + rules.getMin(), "max", "" + rules.getMax()).message();
			
		MMOCore.plugin.configManager.getSimpleMessage("guild-creation.failed", "reason", reason).send(player);
		return false;
	}
}
