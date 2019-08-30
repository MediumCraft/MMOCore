package net.Indyuce.mmocore.comp.entity;

import org.bukkit.entity.Entity;

import de.Keyle.MyPet.api.entity.MyPetBukkitEntity;

public class MyPetEntityHandler implements EntityHandler {

	@Override
	public boolean isCustomEntity(Entity entity) {
		return entity instanceof MyPetBukkitEntity;
	}
}
