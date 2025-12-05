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
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.miauwrijn.gooncraft.Plugin;
import com.miauwrijn.gooncraft.data.PlayerStats;

/**
 * Manages player statistics tracking and persistence.
 * Data is stored in the players folder alongside other player data.
 */
public class StatisticsManager implements Listener {

    private static final Map<UUID, PlayerStats> playerStats = new ConcurrentHashMap<>();
    private static File dataFolder;

    public StatisticsManager() {
        dataFolder = new File(Plugin.instance.getDataFolder(), "players");
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
            Plugin.instance.getLogger().warning("Failed to create players folder: " + dataFolder.getAbsolutePath());
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
                config.getInt("Stats.FapCount", 0),
                config.getInt("Stats.CumOnOthersCount", 0),
                config.getInt("Stats.GotCummedOnCount", 0),
                config.getLong("Stats.TotalTimeWithPenisOut", 0),
                config.getInt("Stats.ButtfingersGiven", 0),
                config.getInt("Stats.ButtfingersReceived", 0),
                config.getInt("Stats.ViagraUsed", 0)
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
            config.set("Stats.FapCount", stats.fapCount);
            config.set("Stats.CumOnOthersCount", stats.cumOnOthersCount);
            config.set("Stats.GotCummedOnCount", stats.gotCummedOnCount);
            config.set("Stats.TotalTimeWithPenisOut", stats.totalTimeWithPenisOut);
            config.set("Stats.ButtfingersGiven", stats.buttfingersGiven);
            config.set("Stats.ButtfingersReceived", stats.buttfingersReceived);
            config.set("Stats.ViagraUsed", stats.viagraUsed);
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

    @EventHandler(priority = EventPriority.LOW)
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
