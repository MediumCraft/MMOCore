package net.Indyuce.mmocore.command.rpg.admin;

import io.lumine.mythic.lib.mmolibcommands.api.CommandTreeNode;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.resource.PlayerResource;
import org.bukkit.command.CommandSender;

public class AdminCommandTreeNode extends CommandTreeNode {
	public AdminCommandTreeNode(CommandTreeNode parent) {
		super(parent, "admin");
		
		addChild(new HideActionBarCommandTreeNode(this));
		addChild(new NoCooldownCommandTreeNode(this));
		addChild(new ResetCommandTreeNode(this));
		addChild(new InfoCommandTreeNode(this));
		addChild(new ClassCommandTreeNode(this));
		addChild(new ForceClassCommandTreeNode(this));

		addChild(new ExperienceCommandTreeNode(this));
		addChild(new LevelCommandTreeNode(this));
		addChild(new AttributeCommandTreeNode(this));

		addChild(new PointsCommandTreeNode("skill", this, PlayerData::setSkillPoints, PlayerData::giveSkillPoints, PlayerData::getSkillPoints));
		addChild(new PointsCommandTreeNode("class", this, PlayerData::setClassPoints, PlayerData::giveClassPoints, PlayerData::getClassPoints));
		addChild(new PointsCommandTreeNode("attribute", this, PlayerData::setAttributePoints, PlayerData::giveAttributePoints, PlayerData::getAttributePoints));
		addChild(new PointsCommandTreeNode("attr-realloc", this, PlayerData::setAttributeReallocationPoints, PlayerData::giveAttributeReallocationPoints, PlayerData::getAttributeReallocationPoints));

		for (PlayerResource res : PlayerResource.values())
			addChild(new ResourceCommandTreeNode(res.name().toLowerCase(), this, res));
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		return CommandResult.THROW_USAGE;
	}
}
