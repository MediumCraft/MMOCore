package net.Indyuce.mmocore.bungee;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.Collection;
import java.util.UUID;

public class MessageListener implements Listener {

    /**
     * Used to register in the cached Players the data that is sent
     */
    @EventHandler
    public void onPluginMessage(PluginMessageEvent e) throws IOException {

        //When a server gives the player data
        if (e.getTag().equals("give_mmocore_player")) {

            byte[] data = e.getData();
            ByteArrayInputStream in = new ByteArrayInputStream(e.getData());
            DataInputStream inputStream = new DataInputStream(in);

            UUID uuid = UUID.fromString(inputStream.readUTF());
            String jsonMsg = inputStream.readUTF();

            //We put this data into the CacheManager
            Bungee.plugin.cacheManager.addCachedPlayer(uuid,jsonMsg);
        }


        //When a server asks for the player data
        if (e.getTag().equals("get_mmocore_player")) {
            byte[] data = e.getData();
            ByteArrayInputStream in = new ByteArrayInputStream(e.getData());
            DataInputStream inputStream = new DataInputStream(in);
            UUID uuid = UUID.fromString(inputStream.readUTF());



            String response=Bungee.plugin.cacheManager.hasCachedPlayer(uuid)?
                    Bungee.plugin.cacheManager.getCachedPlayer(uuid):"{}";
            //We format the data corresponding to the player
            ByteArrayOutputStream out=new ByteArrayOutputStream();
            DataOutputStream outputStream= new DataOutputStream(out);
            outputStream.writeChars(response);

            //We get the corresponding player
            ProxiedPlayer proxiedPlayer= Bungee.plugin.getProxy().getPlayer(uuid);
            //We send the answer
            proxiedPlayer.getServer().getInfo().sendData("get_mmocore_player",out.toByteArray());


        }

    }
}

