package org.windguest.mobwar;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.windguest.mobwar.events.EventsMain;
import org.windguest.mobwar.games.Bounty;
import org.windguest.mobwar.games.Disguise;
import org.windguest.mobwar.games.Entities;
import org.windguest.mobwar.games.Water;
import org.windguest.mobwar.mobs.*;
import org.windguest.mobwar.events.*;
import org.windguest.mobwar.listener.*;

public class Main extends JavaPlugin {

    private static Main instance;

    public static Main getInstance() {
        return instance;
    }

    public void onEnable() {
        instance = this;
        Files.generateFiles();
        Commands commands = new Commands();
        this.getCommand("mobwar").setExecutor(commands);
        this.getCommand("mobwar").setTabCompleter(commands);
        Bukkit.getPluginManager().registerEvents(new ListenerDamage(), this);
        Bukkit.getPluginManager().registerEvents(new ListenerDeath(), this);
        Bukkit.getPluginManager().registerEvents(new ListenerInteract(), this);
        Bukkit.getPluginManager().registerEvents(new ListenerJoin(), this);
        Bukkit.getPluginManager().registerEvents(new ListenerMove(), this);
        Bukkit.getPluginManager().registerEvents(new ListenerOthers(), this);
        Bukkit.getPluginManager().registerEvents(new ListenerProjectile(), this);
        Bukkit.getPluginManager().registerEvents(new ListenerQuit(), this);

        Bukkit.getPluginManager().registerEvents(new Disguise(), this);
        Bukkit.getPluginManager().registerEvents(new Bounty(), this);
        Bukkit.getPluginManager().registerEvents(new Water(), this);
        Bukkit.getPluginManager().registerEvents(new Blaze(), this);
        Bukkit.getPluginManager().registerEvents(new Breeze(), this);
        Bukkit.getPluginManager().registerEvents(new Creeper(), this);
        Bukkit.getPluginManager().registerEvents(new EnderMan(), this);
        Bukkit.getPluginManager().registerEvents(new Evoker(), this);
        Bukkit.getPluginManager().registerEvents(new Ghast(), this);
        Bukkit.getPluginManager().registerEvents(new Goat(), this);
        Bukkit.getPluginManager().registerEvents(new Guardian(), this);
        Bukkit.getPluginManager().registerEvents(new Illusioners(), this);
        Bukkit.getPluginManager().registerEvents(new Piglin(), this);
        Bukkit.getPluginManager().registerEvents(new Skeletons(), this);
        Bukkit.getPluginManager().registerEvents(new Sniffer(), this);
        Bukkit.getPluginManager().registerEvents(new Squid(), this);
        Bukkit.getPluginManager().registerEvents(new Trader(), this);
        Bukkit.getPluginManager().registerEvents(new Vindicator(), this);
        Bukkit.getPluginManager().registerEvents(new Warden(), this);
        Bukkit.getPluginManager().registerEvents(new Wither(), this);
        Bukkit.getPluginManager().registerEvents(new Drowned(), this);

        new Placeholder().register();
        EventsMain.startEvents();
    }

    public void onDisable() {
        Entities.removeAllEntities();
        Spawner.clearSpawnedMonsters();
        Blaze.clearAllFires();
    }
}

