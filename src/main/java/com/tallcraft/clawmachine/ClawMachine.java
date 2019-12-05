package com.tallcraft.clawmachine;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public final class ClawMachine extends JavaPlugin implements Listener {

    private FileConfiguration config;
    private World safeWorld;

    private void initConfig() {
        config = this.getConfig();

        MemoryConfiguration defaultConfig = new MemoryConfiguration();

        defaultConfig.set("unsafeWorlds", new ArrayList<>());
        defaultConfig.set("safeDestinationWorld", "world");

        config.setDefaults(defaultConfig);
        config.options().copyDefaults(true);
        saveConfig();
    }


    @Override
    public void onEnable() {
        initConfig();

        String safeWorldName = config.getString("safeDestinationWorld");

        World safeWorld = Bukkit.getServer().getWorld(safeWorldName);
        if(safeWorld == null) {
            throw new Error("Invalid safeDestinationWorld '" + safeWorldName + "'!");
            // TODO: do we need to disable ourselves?
        }

        getServer().getPluginManager().registerEvents(this, this);
    }


    @EventHandler
    public void onPlayerLoginEvent(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        boolean isUnsafe = config.getStringList("unsafeWorlds").contains(player.getWorld().getName());

        if(!isUnsafe) {
            return;
        }

        player.teleport(safeWorld.getSpawnLocation());
    }

}