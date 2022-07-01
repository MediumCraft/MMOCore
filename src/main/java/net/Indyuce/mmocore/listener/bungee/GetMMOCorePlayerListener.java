package net.Indyuce.mmocore.listener.bungee;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.UUID;

public class GetMMOCorePlayerListener implements PluginMessageListener {
    @Override
    public void onPluginMessageReceived( String channel,  Player player,  byte[] bytes) {
        if(!channel.equals("give_mmocore_player"))
            return;
        ByteArrayDataInput input= ByteStreams.newDataInput(bytes);
        UUID uuid=UUID.fromString(input.readUTF());
        String Json=input.readUTF();
    }


}
