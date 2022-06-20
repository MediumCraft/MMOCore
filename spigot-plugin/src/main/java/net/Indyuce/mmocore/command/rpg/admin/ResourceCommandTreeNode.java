package net.Indyuce.mmocore.command.rpg.admin;

import io.lumine.mythic.lib.command.api.CommandTreeNode;
import io.lumine.mythic.lib.command.api.Parameter;
import net.Indyuce.mmocore.api.quest.trigger.ManaTrigger;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.resource.PlayerResource;
import net.Indyuce.mmocore.command.CommandVerbose;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ResourceCommandTreeNode extends CommandTreeNode {
	private final String type;
	private final PlayerResource resource;

	public ResourceCommandTreeNode(String type, CommandTreeNode parent, PlayerResource resource) {
		super(parent, "resource-" + type);

		this.type = type;
		this.resource = resource;

		addChild(new ActionCommandTreeNode(this, "set", ManaTrigger.Operation.SET));
		addChild(new ActionCommandTreeNode(this, "give", ManaTrigger.Operation.GIVE));
		addChild(new ActionCommandTreeNode(this, "take", ManaTrigger.Operation.TAKE));
	}

	public class ActionCommandTreeNode extends CommandTreeNode {
		private final ManaTrigger.Operation action;

		public ActionCommandTreeNode(CommandTreeNode parent, String type, ManaTrigger.Operation action) {
			super(parent, type);

			this.action = action;

			addParameter(Parameter.PLAYER);
			addParameter(Parameter.AMOUNT);
		}

		@Override
		public CommandResult execute(CommandSender sender, String[] args) {
			if (args.length < 5)
				return CommandResult.THROW_USAGE;

			Player player = Bukkit.getPlayer(args[3]);
			if (player == null) {
				sender.sendMessage(ChatColor.RED + "Could not find the player called " + args[2] + ".");
				return CommandResult.FAILURE;
			}

			double amount;
			try {
				amount = Double.parseDouble(args[4]);
			} catch (Exception e) {
				sender.sendMessage(ChatColor.RED + args[4] + " is not a valid number.");
				return CommandResult.FAILURE;
			}

			PlayerData data = PlayerData.get(player);
			resource.getConsumer(action).accept(data, amount);
			CommandVerbose.verbose(sender, CommandVerbose.CommandType.RESOURCE,
					ChatColor.GOLD + player.getName() + ChatColor.YELLOW + " now has " + ChatColor.GOLD + resource.getCurrent(data)
							+ ChatColor.YELLOW + " " + type + " points.");
			return CommandResult.SUCCESS;
		}
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		return CommandResult.THROW_USAGE;
	}
}
