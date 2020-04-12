package net.Indyuce.mmocore.command.rpg.admin;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.command.api.CommandMap;

public class AdminCommandMap extends CommandMap {
	public AdminCommandMap(CommandMap parent) {
		super(parent, "admin");

		addFloor(new HideActionBarCommandMap(this));
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

		addFloor(new ResourceCommandMap("health", this, (data, value) -> data.getPlayer().setHealth(value), (data, value) -> data.heal(value), (data, value) -> data.heal(-value), (data) -> data.getPlayer().getHealth()));
		addFloor(new ResourceCommandMap("mana", this, (data, value) -> data.setMana(value), (data, value) -> data.giveMana(value), (data, value) -> data.giveMana(-value), (data) -> data.getMana()));
		addFloor(new ResourceCommandMap("stamina", this, (data, value) -> data.setStamina(value), (data, value) -> data.giveStamina(value), (data, value) -> data.giveStamina(-value), (data) -> data.getStamina()));
		addFloor(new ResourceCommandMap("stellium", this, (data, value) -> data.setStellium(value), (data, value) -> data.giveStellium(value), (data, value) -> data.giveStellium(-value), (data) -> data.getStellium()));
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		
		MMOCore.plugin.lootChests.getRegion("test-region").spawnChest(PlayerData.get((Player) sender));
		
		return CommandResult.THROW_USAGE;
	}
}
