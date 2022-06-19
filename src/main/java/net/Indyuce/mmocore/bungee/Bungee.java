package net.Indyuce.mmocore.bungee;

import net.md_5.bungee.api.plugin.Plugin;
import org.checkerframework.checker.units.qual.C;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;

public class Bungee extends Plugin {
    public static Bungee plugin;
    public CacheManager cacheManager = new CacheManager();


    @Override
    public void onEnable() {
        //Register a new communication channel
        getProxy().registerChannel("give_mmocore_player");
        getProxy().registerChannel("get_mmocore_player");
        getProxy().getPluginManager().registerListener(this, new MessageListener());


        try {
            getProxy().getLogger().log(Level.WARNING,"enabling socket");
            ServerSocket serverSocket= new ServerSocket(25580);
            Socket clientSocket=serverSocket.accept();
            getProxy().getLogger().log(Level.WARNING,"port: "+clientSocket.getPort());

            BufferedReader bufferedReader= new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String line;
            while((line=bufferedReader.readLine())!=null) {
                getProxy().getLogger().log(Level.WARNING,line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void onDisable() {

    }

    @Override
    public void onLoad() {
        plugin = this;
    }
}
