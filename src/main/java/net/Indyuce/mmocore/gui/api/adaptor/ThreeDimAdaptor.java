package net.Indyuce.mmocore.gui.api.adaptor;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import io.lumine.mythic.lib.api.util.TemporaryListener;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.gui.api.GeneratedInventory;
import net.Indyuce.mmocore.gui.api.InventoryClickContext;
import net.Indyuce.mmocore.gui.api.item.InventoryItem;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;

public class ThreeDimAdaptor extends Adaptor {
    private final double INITIAL_PERCENTAGE = 0.20;
    private final double INCREMENT_PERCENTAGE = 0.20;

    private SpawnPacketListener packetListener;
    private InteractListener interactListener;
    private final HashMap<Integer, ArmorStand> armorStands = new HashMap<>();

    private final HashSet<ArmorStand> loreArmorStand = new HashSet<>();

    private final HashMap<ItemStack,ArmorStand> hiddenArmorStand =new HashMap<>();

    private boolean firstTime = true;

    private final Vector direction = generated.getPlayer().getEyeLocation().getDirection().setY(0);
    private final Location location = generated.getPlayer().getLocation().add(new Vector(0, generated.getEditable().getVerticalOffset(), 0));

    //Zoomed=-1 no armorstand are under zoom
    private int zoomed = -1;


    public ThreeDimAdaptor(GeneratedInventory generated) {
        super(generated);
    }


    @Override
    public void open() {
        if (!firstTime) {
            fastClose();
            fastOpen();
            return;
        }
        firstTime = false;
        //MMOCore.plugin.protocolManager.addPacketListener(packetListener = new SpawnPacketListener());
        interactListener = new InteractListener();
        for (InventoryItem item : generated.getEditable().getItems()) {
            if (item.canDisplay(generated)) {
                setInventoryItem(item, INITIAL_PERCENTAGE);
            }
        }


        new BukkitRunnable() {
            double total_percentage = INITIAL_PERCENTAGE;

            @Override
            public void run() {
                if (total_percentage < 1) {
                    total_percentage += INCREMENT_PERCENTAGE;
                    for (int slot : armorStands.keySet()) {
                        armorStands.get(slot).teleport(getLocation(slot, total_percentage));
                    }
                } else {
                    cancel();
                }

            }
        }.runTaskTimer(MMOCore.plugin, 0L, 1L);


    }

    @Override
    public void close() {
        //Closes the packet listener,the interact listener and destroys the armor stands.
        //MMOCore.plugin.protocolManager.removePacketListener(packetListener);
        interactListener.close();
        removeLore();

        new BukkitRunnable() {
            double total_percentage = 1;

            @Override
            public void run() {
                if (total_percentage > INITIAL_PERCENTAGE) {
                    total_percentage -= INCREMENT_PERCENTAGE;
                    for (int slot : armorStands.keySet()) {
                        armorStands.get(slot).teleport(getLocation(slot, total_percentage));
                    }
                } else {
                    for (ArmorStand armorStand : armorStands.values())
                        armorStand.remove();
                    cancel();
                }

            }
        }.runTaskTimer(MMOCore.plugin, 0L, 1L);


    }

    /**
     * Opens the inventory without the little animation
     */
    public void fastOpen() {
        //MMOCore.plugin.protocolManager.addPacketListener(packetListener = new SpawnPacketListener());
        interactListener = new InteractListener();
        for (InventoryItem item : generated.getEditable().getItems()) {
            if (item.canDisplay(generated)) {
                setInventoryItem(item, 1);
            }
        }
        if (zoomed != -1) {
            displayLore(zoomed);
            armorStands.get(zoomed).teleport(getLocation(zoomed, 0.75));
        }
    }


    /**
     * Closes the inventory without the little animation
     */
    public void fastClose() {
        //Closes the packet listener,the interact listener and destroys the armor stands.
        //MMOCore.plugin.protocolManager.removePacketListener(packetListener);
        interactListener.close();

        for (ArmorStand armorStand : armorStands.values())
            armorStand.remove();
        removeLore();

    }

    private void setInventoryItem(InventoryItem item, double percentage) {
        generated.addLoaded(item);

        List<Integer> slots = item.getSlots();
        if (item.hasDifferentDisplay()) {
            for (int i : slots) {
                setItem(item.display(generated, i), i, percentage);
            }
        } else {
            ItemStack itemStack = item.display(generated);
            for (int i : slots) {
                setItem(itemStack, i, percentage);
            }
        }


    }

    private void setItem(ItemStack item, int n, double percentage) {
        Location location = getLocation(n, percentage);
        //We create the armorStand corresponding to display the item
        ArmorStand armorStand = (ArmorStand) generated.getPlayer().getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        armorStand.setVisible(false);
        armorStand.setSmall(false);
        armorStand.setArms(true);
        armorStand.setGravity(false);
        armorStand.getEquipment().setItem(EquipmentSlot.HEAD, item);
        if (item.hasItemMeta() && item.getItemMeta().getDisplayName() != null) {
            armorStand.setCustomName(item.getItemMeta().getDisplayName());
        }
        armorStand.setCustomNameVisible(true);

        //We add properties to the PersistentDataContainer of the armor stand
        PersistentDataContainer container = armorStand.getPersistentDataContainer();
        container.set(new NamespacedKey(MMOCore.plugin, "slot"), PersistentDataType.INTEGER, n);


        //Makes the ArmorStand look at you
        //armorStand.setBodyPose(new EulerAngle(-direction.getX(),0,-direction.getZ()));

        armorStands.put(n, armorStand);
    }


    public void displayLore(int n) {
        ArmorStand armorStand = armorStands.get(n);
        ItemStack itemStack = armorStand.getEquipment().getItem(EquipmentSlot.HEAD);

        Location initalLocation = getLocation(n, 1);

        for (ArmorStand armorStand1 : armorStands.values()) {
            armorStand1.setCustomNameVisible(false);
        }

        Vector xAxis=initalLocation.clone().toVector().subtract(generated.getPlayer().getLocation().toVector()).setY(0).normalize();
        Vector yAxis = new Vector(0, 1, 0);
        Vector zAxis = xAxis.clone().rotateAroundAxis(yAxis, -Math.PI / 2);


        //Empiric height of a line: 0.25
        //Empiric size of a char: 0.13
        double lineHeight = 0.25;
        double charSize = 0.09;
        //We search the biggest line in the lore
        int max=0;
        for(String line:itemStack.getItemMeta().getLore()){
            if(line.length()>max)
                max=line.length();
        }



        initalLocation.add(zAxis.clone().multiply(max*charSize/2+0.8)).add(yAxis.clone().multiply(2 + 0.125 * itemStack.getItemMeta().getLore().size()));
        for (String line : itemStack.getItemMeta().getLore()) {
            if (line.length() != 0) {
                ArmorStand as = (ArmorStand) initalLocation.getWorld().spawnEntity(initalLocation, EntityType.ARMOR_STAND);
                as.setSmall(false);
                as.setMarker(true);
                as.setVisible(false);
                as.setCustomNameVisible(true);
                as.setCustomName(line);
                loreArmorStand.add(as);
            }

            initalLocation.add(yAxis.clone().multiply(-lineHeight));

        }

        double totalHeight=lineHeight*itemStack.getItemMeta().getLore().size();
        double totalLength=max*charSize;
        Location topCorner=getLocation(n, 1).add(yAxis.clone().multiply(armorStand.getHeight()*0.25 +totalHeight/2));

        //We remove the items that can be in the field of vision
        for(int slot:armorStands.keySet()) {
            ArmorStand otherArmorStand = armorStands.get(slot);
            if(!otherArmorStand.equals(armorStand)) {
                //Calculates the direction between the player and otherArmorStand
                Vector direction=otherArmorStand.getLocation().add(new Vector(0,0.25*otherArmorStand.getHeight(),0)).subtract(generated.getPlayer().getLocation()).toVector().normalize();
                //The vector between the plan and the player.
                //The plane normal is the xAxis

                double t = (xAxis.clone().dot(topCorner.clone().subtract(generated.getPlayer().getLocation()).toVector())) / xAxis.clone().dot(direction);
                Vector relativeProjection = generated.getPlayer().getLocation().toVector().add(direction.multiply(t)).subtract(topCorner.toVector());


                //The intersection between 'direction' and the plane
                //Vector projection=generated.getPlayer().getLocation().toVector().add(direction.clone().multiply(1/vector.dot(direction)));
                //Vector relativeProjection=projection.subtract(topCorner.toVector());
                double z=zAxis.dot(relativeProjection);
                double y=(n/9-slot/9)*lineHeight+totalHeight/2;
                if(z>0&&z<totalLength&&y>0&&y<totalHeight) {
                    hideArmorStand(otherArmorStand);
                }



            }
        }
    }


    /**
     * Hide the item corrseponding to a certain armor stand if it hides the lore
     */
    public void hideArmorStand(ArmorStand armorStand) {
        hiddenArmorStand.put(armorStand.getEquipment().getItem(EquipmentSlot.HEAD),armorStand);
        armorStand.getEquipment().setItem(EquipmentSlot.HEAD,null);
    }


    /**
     * Reestablishes all the item for armor stand
     */
    public void reestablishHiddenArmorStand() {
        for (ItemStack item: hiddenArmorStand.keySet()) {
            hiddenArmorStand.get(item).getEquipment().setItem(EquipmentSlot.HEAD,item);
        }
        hiddenArmorStand.clear();
    }



    public void removeLore() {
        for (ArmorStand armorStand : loreArmorStand) {
            armorStand.remove();
        }
        loreArmorStand.clear();

        for (ArmorStand armorStand1 : armorStands.values()) {
            armorStand1.setCustomNameVisible(true);
        }
        reestablishHiddenArmorStand();
    }


    public Location getLocation(int n, double percentage) {
        //Determines the location at which the ArmorStand will spawn

        Location cloneLocation = location.clone();
        Vector cloneDirection = direction.clone().rotateAroundAxis(new Vector(0, 1, 0),
                -((n % 9) - 4) * generated.getEditable().getAngleGap() * Math.PI / 180);

        //Curvature of 1: r=cst Curvature of 1: r=R/cos(angle) (a plane)
        double radius = percentage * generated.getEditable().getRadius() / Math.cos((1 - generated.getEditable().getCurvature())
                * -((n % 9) - 4) * generated.getEditable().getAngleGap() * Math.PI / 180);
        cloneDirection = cloneDirection.normalize().multiply(radius);
        cloneDirection.add(new Vector(0, percentage * generated.getEditable().getVerticalGap() * ((generated.getEditable().getSlots() - n - 1) / 9), 1));
        //We get the final direction
        cloneLocation.add(cloneDirection);

        cloneLocation.setDirection(new Vector(-cloneDirection.getX(), 0, -cloneDirection.getZ()));
        return cloneLocation;
    }

    @Override
    public void dynamicallyUpdateItem(InventoryItem<?> item, int n, ItemStack placed, Consumer<ItemStack> update) {

    }

    private class SpawnPacketListener extends PacketAdapter {


        public SpawnPacketListener() {
            super(MMOCore.plugin, PacketType.Play.Server.SPAWN_ENTITY_LIVING);
        }

        /**
         * Cancels all the packet corresponding to an armorStand of the Gui to a player that should not see it.
         */
        @Override
        public void onPacketSending(PacketEvent event) {

            PacketContainer packet = event.getPacket();
            Entity entity = MMOCore.plugin.protocolManager
                    .getEntityFromID(event.getPlayer().getWorld(), packet.getIntegers().read(0));
            if (entity instanceof ArmorStand armorStand) {
                if (true) {
                    Bukkit.broadcastMessage("IN");

                    if (armorStands.values().contains(armorStand)) {
                        Bukkit.broadcastMessage("CANCEL" + armorStand.getName());
                        event.setCancelled(true);
                    }
                }
            }
        }

    }


    private class InteractListener extends TemporaryListener {

        public InteractListener() {
            super(MMOCore.plugin, PlayerInteractAtEntityEvent.getHandlerList()
                    , PlayerMoveEvent.getHandlerList(), PlayerInteractEvent.getHandlerList(), PlayerInteractAtEntityEvent.getHandlerList());
        }

        @EventHandler
        public void onMove(PlayerMoveEvent e) {
            if (e.getPlayer().equals(generated.getPlayer())) {
                if (!e.getFrom().getBlock().getLocation().equals(e.getTo().getBlock().getLocation()))
                    ThreeDimAdaptor.this.close();
                else {
                    //If the player no longer looks at the zoom as:
                    if (zoomed != -1 && generated.getPlayer().getLocation().getDirection().normalize().dot(
                            armorStands.get(zoomed).getLocation().add(new Vector(0, 0.25 * armorStands.get(zoomed).getHeight(), 0))
                                    .subtract(generated.getPlayer().getLocation()).toVector().normalize()) < generated.getEditable().getInteractSensitivity()) {
                        armorStands.get(zoomed).teleport(getLocation(zoomed, 1));
                        zoomed = -1;
                        removeLore();
                    }
                    if (zoomed == -1) {
                        for (int n : armorStands.keySet()) {
                            ArmorStand as = armorStands.get(n);
                            Location asLocation = as.getLocation().add(new Vector(0, 0.25 * as.getHeight(), 0));


                            double scalar = generated.getPlayer().getLocation().getDirection().normalize().dot(
                                    asLocation.subtract(generated.getPlayer().getLocation()).toVector().normalize());

                            if (scalar > generated.getEditable().getInteractSensitivity()) {
                                as.teleport(getLocation(n, 0.75));
                                zoomed = n;
                                displayLore(zoomed);
                            }


                        }

                    }
                }

            }

        }

        @EventHandler
        public void onInteract(PlayerInteractAtEntityEvent event) {
            if (event.getPlayer().equals(generated.getPlayer()))
                if (event.getRightClicked() instanceof ArmorStand armorStand) {
                    if (armorStands.values().contains(armorStand)) {
                        PersistentDataContainer container = armorStand.getPersistentDataContainer();
                        int slot = container.get(new NamespacedKey(MMOCore.plugin, "slot"), PersistentDataType.INTEGER);
                        ClickType clickType;
                        if (event.getPlayer().isSneaking())
                            clickType = ClickType.SHIFT_RIGHT;
                        else
                            clickType = ClickType.RIGHT;
                        generated.whenClicked(new InventoryClickContext(slot, armorStand.getEquipment().getItem(EquipmentSlot.HEAD), clickType, event));
                    }
                }
        }

        @EventHandler
        public void onInteract(PlayerInteractEvent event) {
            if (event.getPlayer().equals(generated.getPlayer())) {
                Player player = event.getPlayer();
                for (ArmorStand armorStand : armorStands.values()) {
                    //Little offset for the armorStand to have the location match the location of the itemstack
                    if (player.getLocation().getDirection().normalize()
                            .dot(armorStand.getLocation().add(new Vector(0, 0.25 * armorStand.getHeight(), 0)).subtract(player.getLocation()).toVector().normalize()) > 0.96) {

                        PersistentDataContainer container = armorStand.getPersistentDataContainer();
                        int slot = container.get(new NamespacedKey(MMOCore.plugin, "slot"), PersistentDataType.INTEGER);
                        ClickType clickType;
                        if (event.getAction() == Action.LEFT_CLICK_AIR) {
                            if (event.getPlayer().isSneaking())
                                clickType = ClickType.SHIFT_LEFT;
                            else
                                clickType = ClickType.LEFT;

                        } else if (event.getAction() == Action.RIGHT_CLICK_AIR) {

                            if (event.getPlayer().isSneaking())
                                clickType = ClickType.SHIFT_RIGHT;
                            else
                                clickType = ClickType.RIGHT;
                        } else {
                            return;
                        }
                        generated.whenClicked(new InventoryClickContext(slot, armorStand.getEquipment().getItem(EquipmentSlot.HEAD), clickType, event));
                        return;
                    }

                }
            }
        }


        @EventHandler
        public void onDamage(EntityDamageByEntityEvent event) {

            if (event.getDamager() instanceof Player player) {
                if (player.equals(generated.getPlayer()))
                    if (event.getEntity() instanceof ArmorStand armorStand) {
                        if (armorStands.values().contains(armorStand)) {
                            PersistentDataContainer container = armorStand.getPersistentDataContainer();
                            int slot = container.get(new NamespacedKey(MMOCore.plugin, "slot"), PersistentDataType.INTEGER);
                            ClickType clickType;
                            if (player.isSneaking())
                                clickType = ClickType.SHIFT_LEFT;
                            else
                                clickType = ClickType.LEFT;

                            ItemStack itemStack = armorStand.getEquipment().getItem(EquipmentSlot.HEAD);
                            generated.whenClicked(new InventoryClickContext(slot, itemStack, clickType, event));
                        }
                    }
            }

        }

        @Override
        public void whenClosed() {


        }

    }
}
