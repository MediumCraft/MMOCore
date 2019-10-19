package net.Indyuce.mmocore.version.nms;

import java.util.Set;
import java.util.UUID;

import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.util.BoundingBox;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import net.Indyuce.mmocore.api.item.NBTItem;
import net.minecraft.server.v1_12_R1.AxisAlignedBB;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.Blocks;
import net.minecraft.server.v1_12_R1.ChatMessage;
import net.minecraft.server.v1_12_R1.ChatMessageType;
import net.minecraft.server.v1_12_R1.Container;
import net.minecraft.server.v1_12_R1.ContainerAnvil;
import net.minecraft.server.v1_12_R1.Entity;
import net.minecraft.server.v1_12_R1.EntityHuman;
import net.minecraft.server.v1_12_R1.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_12_R1.ItemStack;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.PacketPlayOutChat;
import net.minecraft.server.v1_12_R1.PacketPlayOutCloseWindow;
import net.minecraft.server.v1_12_R1.PacketPlayOutOpenWindow;
import net.minecraft.server.v1_12_R1.PacketPlayOutTitle;
import net.minecraft.server.v1_12_R1.PacketPlayOutTitle.EnumTitleAction;
import net.minecraft.server.v1_12_R1.TileEntitySkull;

public class NMSHandler_1_12_R1 implements NMSHandler {
	@Override
	public void sendJson(Player player, String message) {
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutChat(ChatSerializer.a(message)));
	}

	@Override
	public void sendTitle(Player player, String msgTitle, String msgSubTitle, int fadeIn, int ticks, int fadeOut) {
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutTitle(EnumTitleAction.TITLE, ChatSerializer.a("{\"text\": \"" + msgTitle + "\"}")));
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutTitle(EnumTitleAction.SUBTITLE, ChatSerializer.a("{\"text\": \"" + msgSubTitle + "\"}")));
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutTitle(EnumTitleAction.TIMES, null, fadeIn, ticks, fadeOut));
	}

	@Override
	public void sendActionBar(Player player, String message) {
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutChat(ChatSerializer.a("{\"text\": \"" + message + "\"}"), ChatMessageType.GAME_INFO));
	}

	@Override
	public int getNextContainerId(Player player) {
		return ((CraftPlayer) player).getHandle().nextContainerCounter();
	}

	@Override
	public void handleInventoryCloseEvent(Player player) {
		CraftEventFactory.handleInventoryCloseEvent(((CraftPlayer) player).getHandle());
	}

	@Override
	public void sendPacketOpenWindow(Player player, int containerId) {
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutOpenWindow(containerId, "minecraft:anvil", new ChatMessage(Blocks.ANVIL.a() + ".name")));
	}

	@Override
	public void sendPacketCloseWindow(Player player, int containerId) {
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutCloseWindow(containerId));
	}

	@Override
	public void setActiveContainerDefault(Player player) {
		((CraftPlayer) player).getHandle().activeContainer = ((CraftPlayer) player).getHandle().defaultContainer;
	}

	@Override
	public void setActiveContainer(Player player, Object container) {
		((CraftPlayer) player).getHandle().activeContainer = (Container) container;
	}

	@Override
	public void setActiveContainerId(Object container, int containerId) {
		((Container) container).windowId = containerId;
	}

	@Override
	public void addActiveContainerSlotListener(Object container, Player player) {
		((Container) container).addSlotListener(((CraftPlayer) player).getHandle());
	}

	@Override
	public Inventory toBukkitInventory(Object container) {
		return ((Container) container).getBukkitView().getTopInventory();
	}

	@Override
	public Object newContainerAnvil(Player player) {
		return new AnvilContainer(((CraftPlayer) player).getHandle());
	}

	private class AnvilContainer extends ContainerAnvil {
		public AnvilContainer(EntityHuman entityhuman) {
			super(entityhuman.inventory, entityhuman.world, new BlockPosition(0, 0, 0), entityhuman);
			this.checkReachable = false;
		}
	}

	@Override
	public NBTItem getNBTItem(org.bukkit.inventory.ItemStack item) {
		return new NBTItem_v1_13_1(item);
	}

	public class NBTItem_v1_13_1 extends NBTItem {
		private final ItemStack nms;
		private final NBTTagCompound compound;

		public NBTItem_v1_13_1(org.bukkit.inventory.ItemStack item) {
			super(item);
			nms = CraftItemStack.asNMSCopy(item);
			compound = nms.hasTag() ? nms.getTag() : new NBTTagCompound();
		}

		@Override
		public String getString(String path) {
			return compound.getString(path);
		}

		@Override
		public boolean has(String path) {
			return compound.hasKey(path);
		}

		@Override
		public boolean getBoolean(String path) {
			return compound.getBoolean(path);
		}

		@Override
		public double getDouble(String path) {
			return compound.getDouble(path);
		}

		@Override
		public int getInt(String path) {
			return compound.getInt(path);
		}

		@Override
		public NBTItem add(ItemTag... tags) {
			for (ItemTag tag : tags) {
				if (tag.getValue() instanceof Boolean)
					compound.setBoolean(tag.getPath(), (boolean) tag.getValue());
				else if (tag.getValue() instanceof Double)
					compound.setDouble(tag.getPath(), (double) tag.getValue());
				else if (tag.getValue() instanceof String)
					compound.setString(tag.getPath(), (String) tag.getValue());
				else if (tag.getValue() instanceof Integer)
					compound.setInt(tag.getPath(), (int) tag.getValue());
			}
			return this;
		}

		@Override
		public NBTItem remove(String... paths) {
			for (String path : paths)
				compound.remove(path);
			return this;
		}

		@Override
		public Set<String> getTags() {
			return compound.c();
		}

		@Override
		public org.bukkit.inventory.ItemStack toItem() {
			nms.setTag(compound);
			return CraftItemStack.asBukkitCopy(nms);
		}
	}

	@Override
	public BoundingBox getBoundingBox(org.bukkit.entity.Entity target) {
		AxisAlignedBB aabb = ((Entity) target).getBoundingBox();
		return new BoundingBox(aabb.a, aabb.b, aabb.c, aabb.d, aabb.e, aabb.f);
	}

	@Override
	public String getSkullValue(Block block) {
		TileEntitySkull skullTile = (TileEntitySkull)((CraftWorld)block.getWorld()).getHandle().getTileEntity(new BlockPosition(block.getX(), block.getY(), block.getZ()));
		if(skullTile.getGameProfile() == null) return "";
		return skullTile.getGameProfile().getProperties().get("textures").iterator().next().getValue();
	}

	@Override
	public void setSkullValue(Block block, String value) {
		TileEntitySkull skullTile = (TileEntitySkull)((CraftWorld)block.getWorld()).getHandle().getTileEntity(new BlockPosition(block.getX(), block.getY(), block.getZ()));
		GameProfile profile = new GameProfile(UUID.randomUUID(), null);
		profile.getProperties().put("textures", new Property("textures", value));
		skullTile.setGameProfile(profile);
		skullTile.update();
	}
}
