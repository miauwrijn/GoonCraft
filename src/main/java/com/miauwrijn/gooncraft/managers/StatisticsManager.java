package com.miauwrijn.gooncraft.managers;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.miauwrijn.gooncraft.Plugin;
import com.miauwrijn.gooncraft.data.PlayerStats;

/**
 * Manages player statistics tracking and persistence.
 */
public class StatisticsManager implements Listener {

    private static final Map<UUID, PlayerStats> playerStats = new ConcurrentHashMap<>();
    private final File dataFolder;

    public StatisticsManager() {
        this.dataFolder = new File(Plugin.instance.getDataFolder(), "stats");
        ensureDataFolderExists();
        loadOnlinePlayers();
        
        Bukkit.getPluginManager().registerEvents(this, Plugin.instance);
        
        // Auto-save every 5 minutes
        Bukkit.getScheduler().runTaskTimerAsynchronously(Plugin.instance, this::saveAll, 6000L, 6000L);
    }

    public static PlayerStats getStats(Player player) {
        return playerStats.computeIfAbsent(player.getUniqueId(), k -> new PlayerStats());
    }

    public static PlayerStats getStats(UUID uuid) {
        return playerStats.get(uuid);
    }

    // ===== Stat Increment Methods =====

    public static void incrementFapCount(Player player) {
        PlayerStats stats = getStats(player);
        stats.fapCount++;
        AchievementManager.checkAchievements(player, stats);
    }

    public static void incrementCumOnOthers(Player player) {
        PlayerStats stats = getStats(player);
        stats.cumOnOthersCount++;
        AchievementManager.checkAchievements(player, stats);
    }

    public static void incrementGotCummedOn(Player player) {
        PlayerStats stats = getStats(player);
        stats.gotCummedOnCount++;
        AchievementManager.checkAchievements(player, stats);
    }

    public static void incrementButtfingersGiven(Player player) {
        PlayerStats stats = getStats(player);
        stats.buttfingersGiven++;
        AchievementManager.checkAchievements(player, stats);
    }

    public static void incrementButtfingersReceived(Player player) {
        PlayerStats stats = getStats(player);
        stats.buttfingersReceived++;
        AchievementManager.checkAchievements(player, stats);
    }

    public static void incrementViagraUsed(Player player) {
        PlayerStats stats = getStats(player);
        stats.viagraUsed++;
        AchievementManager.checkAchievements(player, stats);
    }

    public static void startPenisOutTimer(Player player) {
        PlayerStats stats = getStats(player);
        stats.startPenisOutTimer();
    }

    public static void stopPenisOutTimer(Player player) {
        PlayerStats stats = getStats(player);
        stats.stopPenisOutTimer();
        AchievementManager.checkAchievements(player, stats);
    }

    // ===== Persistence =====

    private void ensureDataFolderExists() {
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            Plugin.instance.getLogger().warning("Failed to create stats folder: " + dataFolder.getAbsolutePath());
        }
    }

    private void loadOnlinePlayers() {
        for (Player player : Plugin.instance.getServer().getOnlinePlayers()) {
            loadPlayerStats(player);
        }
    }

    private void loadPlayerStats(Player player) {
        File file = new File(dataFolder, player.getUniqueId() + ".yml");
        
        if (file.exists()) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            
            PlayerStats stats = new PlayerStats(
                config.getInt("fapCount", 0),
                config.getInt("cumOnOthersCount", 0),
                config.getInt("gotCummedOnCount", 0),
                config.getLong("totalTimeWithPenisOut", 0),
                config.getInt("buttfingersGiven", 0),
                config.getInt("buttfingersReceived", 0),
                config.getInt("viagraUsed", 0)
            );
            
            playerStats.put(player.getUniqueId(), stats);
        } else {
            playerStats.put(player.getUniqueId(), new PlayerStats());
        }
    }

    private void savePlayerStats(Player player) {
        PlayerStats stats = playerStats.get(player.getUniqueId());
        if (stats == null) return;
        
        // Stop timer to capture current session time
        if (stats.isPenisOut) {
            stats.stopPenisOutTimer();
        }
        
        File file = new File(dataFolder, player.getUniqueId() + ".yml");
        
        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            config.set("fapCount", stats.fapCount);
            config.set("cumOnOthersCount", stats.cumOnOthersCount);
            config.set("gotCummedOnCount", stats.gotCummedOnCount);
            config.set("totalTimeWithPenisOut", stats.totalTimeWithPenisOut);
            config.set("buttfingersGiven", stats.buttfingersGiven);
            config.set("buttfingersReceived", stats.buttfingersReceived);
            config.set("viagraUsed", stats.viagraUsed);
            config.save(file);
        } catch (IOException e) {
            Plugin.instance.getLogger().log(Level.WARNING, "Failed to save player stats", e);
        }
    }

    private void saveAll() {
        for (Player player : Plugin.instance.getServer().getOnlinePlayers()) {
            savePlayerStats(player);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        loadPlayerStats(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerStats stats = playerStats.get(player.getUniqueId());
        
        if (stats != null && stats.isPenisOut) {
            stats.stopPenisOutTimer();
        }
        
        savePlayerStats(player);
        playerStats.remove(player.getUniqueId());
    }
}
