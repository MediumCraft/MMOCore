package net.Indyuce.mmocore.command.rpg.admin;

import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.command.CommandSender;

import io.lumine.mythic.lib.mmolibcommands.api.CommandTreeNode;

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

		addChild(new ResourceCommandTreeNode("health", this, (data, value) -> data.getPlayer().setHealth(value), PlayerData::heal, (data, value) -> data.heal(-value), (data) -> data.getPlayer().getHealth()));
		addChild(new ResourceCommandTreeNode("mana", this, PlayerData::setMana, PlayerData::giveMana, (data, value) -> data.giveMana(-value), PlayerData::getMana));
		addChild(new ResourceCommandTreeNode("stamina", this, PlayerData::setStamina, PlayerData::giveStamina, (data, value) -> data.giveStamina(-value), PlayerData::getStamina));
		addChild(new ResourceCommandTreeNode("stellium", this, PlayerData::setStellium, PlayerData::giveStellium, (data, value) -> data.giveStellium(-value), PlayerData::getStellium));
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		return CommandResult.THROW_USAGE;
	}
}
