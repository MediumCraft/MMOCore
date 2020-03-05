package net.Indyuce.mmocore;
//package fuck.im.so.dead;

import java.util.logging.Level;

import org.bukkit.Bukkit;
//import plz.dont.hurt.me.Indy;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;

import net.Indyuce.mmocore.comp.mythicmobs.MythicMobsDrops;
//Wooooow

public class IndyPlz /**This is for fixing a bug, not just stupid code, plz plz plz*/ implements Listener /**here my lovely indy, it's a bug fix*/ {
	@EventHandler //For handling this very cool class
	public void iloveyoupleasedonthurtme(PluginEnableEvent /**See this event is super cool, but I understand why you'd dislike this "fix"*/ imsorryindy) {
		if (imsorryindy.getPlugin().getName().equals("MythicMobs")) /**The load order issue isn't caused by anyone, but I wish we could have an easier fix*/ {
			Bukkit.getServer().getPluginManager().registerEvents(new MythicMobsDrops(), MMOCore.plugin);
			//Can you even safely register an event within a listener and an eventhandler method?? I don't know, but it works for now! 
			MMOCore.plugin.getLogger().log(Level.INFO, "Hooked onto MythicMobs"); //PLEASE LOG MY DEATH WHEN INDY SLITS MY THROAT, I'M SCARED
		}
	}
}

//Love from Aria