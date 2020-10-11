package net.Indyuce.mmocore.command.rpg.admin;

import org.bukkit.command.CommandSender;

import net.mmogroup.mmolib.command.api.CommandTreeNode;

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

		addChild(new PointsCommandTreeNode("skill", this, (data, points) -> data.setSkillPoints(points), (data, points) -> data.giveSkillPoints(points), (data) -> data.getSkillPoints()));
		addChild(new PointsCommandTreeNode("class", this, (data, points) -> data.setClassPoints(points), (data, points) -> data.giveClassPoints(points), (data) -> data.getClassPoints()));
		addChild(new PointsCommandTreeNode("attribute", this, (data, points) -> data.setAttributePoints(points), (data, points) -> data.giveAttributePoints(points), (data) -> data.getAttributePoints()));
		addChild(new PointsCommandTreeNode("attr-realloc", this, (data, points) -> data.setAttributeReallocationPoints(points), (data, points) -> data.giveAttributeReallocationPoints(points), (data) -> data.getAttributeReallocationPoints()));

		addChild(new ResourceCommandTreeNode("health", this, (data, value) -> data.getPlayer().setHealth(value), (data, value) -> data.heal(value), (data, value) -> data.heal(-value), (data) -> data.getPlayer().getHealth()));
		addChild(new ResourceCommandTreeNode("mana", this, (data, value) -> data.setMana(value), (data, value) -> data.giveMana(value), (data, value) -> data.giveMana(-value), (data) -> data.getMana()));
		addChild(new ResourceCommandTreeNode("stamina", this, (data, value) -> data.setStamina(value), (data, value) -> data.giveStamina(value), (data, value) -> data.giveStamina(-value), (data) -> data.getStamina()));
		addChild(new ResourceCommandTreeNode("stellium", this, (data, value) -> data.setStellium(value), (data, value) -> data.giveStellium(value), (data, value) -> data.giveStellium(-value), (data) -> data.getStellium()));
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		return CommandResult.THROW_USAGE;
	}
}
