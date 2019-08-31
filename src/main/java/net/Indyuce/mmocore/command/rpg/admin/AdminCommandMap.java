package net.Indyuce.mmocore.command.rpg.admin;

import org.bukkit.command.CommandSender;

import net.Indyuce.mmocore.command.api.CommandMap;

public class AdminCommandMap extends CommandMap {
	public AdminCommandMap(CommandMap parent) {
		super(parent, "admin");

		addFloor(new NoCooldownCommandMap(this));
		addFloor(new ResetCommandMap(this));
		addFloor(new InfoCommandMap(this));
		addFloor(new ClassCommandMap(this));

		addFloor(new ExperienceCommandMap(this));
		addFloor(new LevelCommandMap(this));

		addFloor(new PointsCommandMap("skill", this, (data, points) -> data.setSkillPoints(points), (data, points) -> data.giveSkillPoints(points), (data) -> data.getSkillPoints()));
		addFloor(new PointsCommandMap("class", this, (data, points) -> data.setClassPoints(points), (data, points) -> data.giveClassPoints(points), (data) -> data.getClassPoints()));
		addFloor(new PointsCommandMap("attribute", this, (data, points) -> data.setAttributePoints(points), (data, points) -> data.giveAttributePoints(points), (data) -> data.getAttributePoints()));
		addFloor(new PointsCommandMap("attr-realloc", this, (data, points) -> data.setAttributeReallocationPoints(points), (data, points) -> data.giveAttributeReallocationPoints(points), (data) -> data.getAttributeReallocationPoints()));
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		return CommandResult.THROW_USAGE;
	}
}
