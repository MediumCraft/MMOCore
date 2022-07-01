package net.Indyuce.mmocore.listener.option;

import io.lumine.mythic.lib.UtilityMethods;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.event.PlayerKeyPressEvent;
import net.Indyuce.mmocore.api.SoundEvent;
import net.Indyuce.mmocore.skill.cast.PlayerKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class HotbarSwap implements Listener {
    private final PlayerKey keybind;
    private final boolean crouching;

    public HotbarSwap(ConfigurationSection config) {
        this.keybind = PlayerKey.valueOf(UtilityMethods.enumName(Objects.requireNonNull(config.getString("keybind"), "Could not find keybind")));
        this.crouching = config.getBoolean("crouching");
    }

    @EventHandler
    public void keyPress(PlayerKeyPressEvent event) {
        Player player = event.getPlayer();
        if (event.getPressed() == keybind && event.getPlayer().isSneaking() == crouching) {

            if (event.getPressed().shouldCancelEvent())
                event.setCancelled(true);

            MMOCore.plugin.soundManager.getSound(SoundEvent.HOTBAR_SWAP).playTo(player);
            for (int j = 0; j < 9; j++) {
                ItemStack replaced = player.getInventory().getItem(j + 9 * 3);
                player.getInventory().setItem(j + 9 * 3, player.getInventory().getItem(j));
                player.getInventory().setItem(j, replaced);
            }
        }
    }
}
