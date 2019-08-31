package net.Indyuce.mmocore.comp;

import org.bukkit.entity.Entity;

import com.nisovin.shopkeepers.api.ShopkeepersPlugin;

import net.Indyuce.mmocore.comp.entity.EntityHandler;

public class ShopKeepersEntityHandler implements EntityHandler {

	@Override
	public boolean isCustomEntity(Entity entity) {
		return ShopkeepersPlugin.getInstance().getShopkeeperRegistry().isShopkeeper(entity);
	}
}
