package com.tallcraft.clawmachine;

import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public final class ClawMachine extends JavaPlugin implements Listener {

    private FileConfiguration config;
    private World safeWorld;
    private boolean listenLogin;
    private boolean listenRespawn;

    private void initConfig() {
        config = this.getConfig();

        MemoryConfiguration defaultConfig = new MemoryConfiguration();

        defaultConfig.set("unsafeWorlds", new ArrayList<>());
        defaultConfig.set("safeDestinationWorld", "world");
        defaultConfig.set("teleportOnLogin", true);
        defaultConfig.set("teleportOnRespawn", true);
        defaultConfig.set("teleportMsg", "&6You have left &9{{UnsafeWorldName}}&6.");
        defaultConfig.set("unsafeWorldEnterMsg", "&6You are entering &9{{UnsafeWorldName}}&6. Please note that you will automatically leave this world on logout or death.");

        config.setDefaults(defaultConfig);
        config.options().copyDefaults(true);
        saveConfig();
    }


    @Override
    public void onEnable() {
        initConfig();

        String safeWorldName = config.getString("safeDestinationWorld");
        if (safeWorldName == null) {
            getLogger().warning("Missing safeDestinationWorld name! Please check the configuration file");
            Bukkit.getPluginManager().disablePlugin(this);
        }

        safeWorld = Bukkit.getServer().getWorld(safeWorldName);
        if (safeWorld == null) {
            getLogger().warning("Invalid safeDestinationWorld '" + safeWorldName + "'!");
            Bukkit.getPluginManager().disablePlugin(this);
        }

        listenLogin = config.getBoolean("teleportOnLogin");
        listenRespawn = config.getBoolean("teleportOnRespawn");

        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPlayerChangedWorldEvent(PlayerChangedWorldEvent event) {
        String worldEnterMsg = config.getString("unsafeWorldEnterMsg");

        if (worldEnterMsg != null && !worldEnterMsg.isEmpty()) {
            Player player = event.getPlayer();
            String destWorldName = player.getWorld().getName();
            if (config.getStringList("unsafeWorlds").contains(destWorldName)) {
                String msg = ChatColor.translateAlternateColorCodes('&',
                        worldEnterMsg.replace("{{UnsafeWorldName}}", destWorldName));
                player.sendMessage(msg);
            }
        }
    }

    private void maybeTeleportPlayer(Player player) {
        String currentWorldName = player.getWorld().getName();
        boolean isUnsafe = config.getStringList("unsafeWorlds").contains(currentWorldName);

        if (!isUnsafe) {
            return;
        }

        String configMsg = config.getString("teleportMsg");
        String msg = null;
        if (configMsg != null && !configMsg.isEmpty()) {
            msg = configMsg.replace("{{UnsafeWorldName}}", currentWorldName);
            msg = ChatColor.translateAlternateColorCodes('&', msg);
        }
        PaperLib.teleportAsync(player, safeWorld.getSpawnLocation());
        if (msg != null) {
            player.sendMessage(msg);
        }
    }

    @EventHandler
    public void onPlayerRespawnEvent(PlayerRespawnEvent event) {
        if (!listenRespawn) {
            return;
        }
        maybeTeleportPlayer(event.getPlayer());
    }


    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        if (!listenLogin) {
            return;
        }
        maybeTeleportPlayer(event.getPlayer());
    }
}
