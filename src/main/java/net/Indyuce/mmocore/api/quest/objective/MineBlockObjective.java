package net.Indyuce.mmocore.api.quest.objective;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import net.Indyuce.mmocore.api.event.CustomBlockMineEvent;
import net.Indyuce.mmocore.api.quest.ObjectiveProgress;
import net.Indyuce.mmocore.api.quest.QuestProgress;
import net.mmogroup.mmolib.api.MMOLineConfig;

public class MineBlockObjective extends Objective {
	private final Material block;
	private final int required;
	private final boolean playerPlaced;

	public MineBlockObjective(ConfigurationSection section, MMOLineConfig config) {
		super(section);

		config.validate("type", "amount");

		block = Material.valueOf(config.getString("type").replace("-", "_").toUpperCase());
		required = config.getInt("amount");
		playerPlaced = config.getBoolean("player-placed", false);
	}

	@Override
	public ObjectiveProgress newProgress(QuestProgress questProgress) {
		return new MineBlockProgress(questProgress, this);
	}

	public class MineBlockProgress extends ObjectiveProgress implements Listener {
		private int count;

		public MineBlockProgress(QuestProgress questProgress, Objective objective) {
			super(questProgress, objective);
		}

		@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
		public void a(BlockBreakEvent event) {
			if ((!playerPlaced) && event.getBlock().hasMetadata("player_placed"))
				return;
			if (event.getPlayer().equals(getQuestProgress().getPlayer().getPlayer()) && event.getBlock().getType() == block) {
				count++;
				getQuestProgress().getPlayer().getQuestData().updateBossBar();
				if (count >= required)
					getQuestProgress().completeObjective();
			}
		}

		@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
		public void b(CustomBlockMineEvent event) {
			if (event.getPlayer().equals(getQuestProgress().getPlayer().getPlayer()) && event.getBlock().getType() == block) {
				count++;
				getQuestProgress().getPlayer().getQuestData().updateBossBar();
				if (count >= required)
					getQuestProgress().completeObjective();
			}
		}

		@Override
		public String formatLore(String lore) {
			return lore.replace("{left}", "" + (required - count));
		}
	}
}
