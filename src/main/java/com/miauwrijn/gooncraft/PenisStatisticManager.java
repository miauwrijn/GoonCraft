package com.miauwrijn.gooncraft;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;

import com.miauwrijn.gooncraft.data.PenisStatistics;
import com.miauwrijn.gooncraft.models.PenisModel;

public class PenisStatisticManager implements Listener {

    static HashMap<UUID, PenisStatistics> penisStatistics = new HashMap<>();

    static PenisStatistics getStatistics(Player player) {
        UUID uuid = player.getUniqueId();
        return penisStatistics.get(uuid);
    }

    static void setViagraBoost(Player player, int boost) {
        UUID uuid = player.getUniqueId();
        PenisStatistics statistics = penisStatistics.get(uuid);
        statistics.viagraBoost = boost;
        penisStatistics.put(uuid, statistics);
        statistics.penisModel.reload(statistics);
    }

    static void setPenisSize(Player player, int size) {
        UUID uuid = player.getUniqueId();
        PenisStatistics statistics = penisStatistics.get(uuid);
        statistics.size = size;
        penisStatistics.put(uuid, statistics);

        File file = new File(new File(Plugin.instance.getDataFolder(), "players"),
                player.getUniqueId().toString() + ".yml");
        try {

            FileConfiguration playerData = YamlConfiguration.loadConfiguration(file);
            playerData.set("Penis.Size", size);
            playerData.save(file);
        } catch (Exception e) {
            Plugin.instance.getLogger().warning("Error saving file: " + file.getAbsolutePath());
        }

        if (statistics.penisModel != null) {
            statistics.penisModel.reload(statistics);
        }
    }

    static void setPenisGirth(Player player, int girth) {
        UUID uuid = player.getUniqueId();
        PenisStatistics statistics = penisStatistics.get(uuid);
        statistics.girth = girth;
        penisStatistics.put(uuid, statistics);

        File file = new File(new File(Plugin.instance.getDataFolder(), "players"),
                player.getUniqueId().toString() + ".yml");
        try {

            FileConfiguration playerData = YamlConfiguration.loadConfiguration(file);
            playerData.set("Penis.Girth", girth);
            playerData.save(file);
        } catch (Exception e) {
            Plugin.instance.getLogger().warning("Error saving file: " + file.getAbsolutePath());
        }

        if (statistics.penisModel != null) {
            statistics.penisModel.reload(statistics);
        }
    }

    static void setPenisBbc(Player player, boolean bbc) {
        UUID uuid = player.getUniqueId();
        PenisStatistics statistics = penisStatistics.get(uuid);
        statistics.bbc = bbc;
        penisStatistics.put(uuid, statistics);

        File file = new File(new File(Plugin.instance.getDataFolder(), "players"),
                player.getUniqueId().toString() + ".yml");
        try {

            FileConfiguration playerData = YamlConfiguration.loadConfiguration(file);
            playerData.set("Penis.BBC", bbc);
            playerData.save(file);
        } catch (Exception e) {
            Plugin.instance.getLogger().warning("Error saving file: " + file.getAbsolutePath());
        }

        if (statistics.penisModel != null) {
            statistics.penisModel.reload(statistics);
        }
    }

    static void setActivePenis(Player player, PenisModel penisModel, int runnableTaskId) {
        PenisStatistics stats = penisStatistics.get(player.getUniqueId());
        stats.penisModel = penisModel;
        stats.runnableTaskId = runnableTaskId;
    }

    static void clearActivePenis(Player player) {
        PenisStatistics stats = penisStatistics.get(player.getUniqueId());
        if (stats != null) {
            if (stats.penisModel != null) {
                stats.penisModel.discard();
                stats.penisModel = null;
                // stop this runnable
                Bukkit.getScheduler().cancelTask(stats.runnableTaskId);
            }
        }
    }

    File dataFolder;

    public PenisStatisticManager() {
        dataFolder = new File(Plugin.instance.getDataFolder(), "players");
        makeDirs();

        // re-register statistics from logged in players on reload
        syncStatistics();

        PluginManager pm = Bukkit.getServer().getPluginManager();
        pm.registerEvents(this, Plugin.instance);

    }

    private void syncStatistics() {

        // get all players who are online
        Collection<? extends Player> players = Plugin.instance.getServer().getOnlinePlayers();
        for (Player player : players) {
            clearActivePenis(player);
            penisStatistics.remove(player.getUniqueId());
            createPlayerData(player);
        }
        // clear all active penises in hashmap

        // clear floating dicks
        // Get all BlockDisplayEntities in the servr
        List<World> worlds = Plugin.instance.getServer().getWorlds();
        for (World world : worlds) {
            List<Entity> entities = world.getEntities();
            for (Entity entity : entities) {
                if (entity.getType() == EntityType.BLOCK_DISPLAY) {
                    if (entity.getName().equalsIgnoreCase("GOONCRAFT")) {
                        entity.remove();
                    }
                }
            }
        }

    }

    public void makeDirs() {
        if (!dataFolder.exists()) {
            try {
                boolean created = dataFolder.mkdirs();
                if (!created) {
                    Plugin.instance.getLogger().warning("Error creating directory: " + dataFolder.getAbsolutePath());
                }
            } catch (Exception e) {
                Plugin.instance.getLogger().warning("Error creating directory: " + dataFolder.getAbsolutePath());
            }
        }
    }

    private void createPlayerData(Player player) {
        File file = new File(dataFolder, player.getUniqueId().toString() + ".yml");
        if (file.exists()) {
            // load player data
            FileConfiguration playerData = YamlConfiguration.loadConfiguration(file);
            int size = playerData.getInt("Penis.Size", -1);
            if (size == -1) {
                size = PenisModel.getRandomSize();
                playerData.set("Penis.Size", size);
                try {
                    playerData.save(file);
                } catch (IOException e) {
                    Plugin.instance.getLogger().warning("Error saving file: " + file.getAbsolutePath());
                }
            }

            int girth = playerData.getInt("Penis.Girth", -1);
            if (girth == -1) {
                girth = PenisModel.getRandomGirth();
                playerData.set("Penis.Girth", girth);
                try {
                    playerData.save(file);
                } catch (IOException e) {
                    Plugin.instance.getLogger().warning("Error saving file: " + file.getAbsolutePath());
                }
            }

            boolean bbc = playerData.getBoolean("Penis.BBC");

            penisStatistics.put(player.getUniqueId(), new PenisStatistics(size, girth, bbc));

        } else {
            try {
                boolean created = file.createNewFile();
                if (created) {
                    int randomSize = PenisModel.getRandomSize();
                    int randomGirth = PenisModel.getRandomGirth();
                    boolean randomBbc = PenisModel.getRandomBbc();
                    // set variable penis size in this yaml to random value between
                    // PenisModel.minSize and PenisModel.maxSize
                    FileConfiguration playerData = YamlConfiguration.loadConfiguration(file);
                    playerData.createSection("Penis");
                    playerData.set("Penis.Size", randomSize);
                    playerData.set("Penis.Girth", randomGirth);
                    playerData.set("Penis.BBC", randomBbc);
                    playerData.save(file);

                    penisStatistics.put(player.getUniqueId(), new PenisStatistics(randomSize, randomGirth, randomBbc));
                }
            } catch (Exception e) {
                Plugin.instance.getLogger().warning("Error creating file: " + file.getAbsolutePath());
            }
        }

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // check if there's a yaml file with player guid
        Player player = event.getPlayer();
        createPlayerData(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        clearActivePenis(player);
    }

    @EventHandler
    public void onPlayerInteract(PlayerAnimationEvent event) {
        if (event.getAnimationType() == PlayerAnimationType.ARM_SWING) {
            Player player = event.getPlayer();
            // check if item is not holding anything
            if (player.getInventory().getItemInMainHand().getType() == Material.AIR) {
                PenisStatistics statistics = getStatistics(player);
                if (statistics.penisModel != null) {
                    statistics.penisModel.cum();
                }
            }
        }
    }

}

