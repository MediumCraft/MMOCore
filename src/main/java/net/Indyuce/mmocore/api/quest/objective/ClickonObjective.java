package net.Indyuce.mmocore.api.quest.objective;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import net.Indyuce.mmocore.api.quest.ObjectiveProgress;
import net.Indyuce.mmocore.api.quest.QuestProgress;
import io.lumine.mythic.lib.api.MMOLineConfig;

public class ClickonObjective extends Objective {
	private final Location loc;
	private final int rangeSquared;

	public ClickonObjective(ConfigurationSection section, MMOLineConfig config) {
		super(section);

		config.validate("range", "world", "x", "y", "z");

		rangeSquared = config.getInt("range") ^ 2;

		World world = Bukkit.getWorld(config.getString("world"));
		Validate.notNull(world, "Could not find world " + config.getString("world"));
		loc = new Location(world, config.getInt("x"), config.getInt("y"), config.getInt("z"));
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
		public void a(PlayerInteractEvent event) {
			if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
				return;

			Player player = event.getPlayer();
			if (getPlayer().isOnline() && player.equals(getPlayer().getPlayer()))
				if (player.getWorld().equals(loc.getWorld()) && event.getClickedBlock().getLocation().distanceSquared(loc) < rangeSquared)
					getQuestProgress().completeObjective();
		}

		@Override
		public String formatLore(String lore) {
			return lore;
		}
	}
}
