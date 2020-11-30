package net.Indyuce.mmocore.comp.citizens;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmocore.api.quest.ObjectiveProgress;
import net.Indyuce.mmocore.api.quest.QuestProgress;
import net.Indyuce.mmocore.api.quest.objective.Objective;
import net.mmogroup.mmolib.api.MMOLineConfig;

public class GetItemObjective extends Objective {
	private final Material material;
	private final int required;
	private final int npcId;

	public GetItemObjective(ConfigurationSection section, MMOLineConfig config) {
		super(section);

		config.validate("type", "amount", "npc");

		material = Material.valueOf(config.getString("type").toUpperCase().replace("-", "_"));
		required = config.getInt("amount");
		npcId = config.getInt("npc");
	}

	@Override
	public ObjectiveProgress newProgress(QuestProgress questProgress) {
		return new GotoProgress(questProgress, this);
	}

	public class GotoProgress extends ObjectiveProgress implements Listener {
		public GotoProgress(QuestProgress questProgress, Objective objective) {
			super(questProgress, objective);
		}

		@EventHandler
		public void a(CitizenInteractEvent event) {
			Player player = event.getPlayer();
			if(!getQuestProgress().getPlayer().isOnline()) return;
			if (player.equals(getQuestProgress().getPlayer().getPlayer()) && event.getNPC().getId() == npcId && player.getInventory().getItemInMainHand() != null) {
				ItemStack item = player.getInventory().getItemInMainHand();
				if (item.getType() == material && item.getAmount() >= required) {
					item.setAmount(item.getAmount() - required);
					getQuestProgress().completeObjective();
				}
			}
		}

		@Override
		public String formatLore(String lore) {
			return lore;
		}
	}
}
