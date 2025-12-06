package com.miauwrijn.gooncraft.managers;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerQuitEvent;

import com.miauwrijn.gooncraft.Plugin;
import com.miauwrijn.gooncraft.data.PenisStatistics;
import com.miauwrijn.gooncraft.models.PenisModel;
import com.miauwrijn.gooncraft.storage.PlayerData;
import com.miauwrijn.gooncraft.storage.StorageManager;

/**
 * Manages penis models and runtime statistics.
 * Uses StorageManager for persistence (supports file and database storage).
 */
public class PenisStatisticManager implements Listener {

    // Runtime-only state: active models and task IDs (not persisted)
    private static final Map<UUID, PenisStatistics> runtimeStats = new ConcurrentHashMap<>();

    public PenisStatisticManager() {
        // Clean up any floating models from previous session
        cleanupFloatingModels();
        
        Bukkit.getPluginManager().registerEvents(this, Plugin.instance);
    }

    /**
     * Get runtime statistics (including active model) for a player.
     * Creates one if it doesn't exist.
     */
    public static PenisStatistics getStatistics(Player player) {
        return runtimeStats.computeIfAbsent(player.getUniqueId(), k -> {
            PlayerData data = StorageManager.getPlayerData(player);
            return new PenisStatistics(data.penisSize, data.penisGirth, data.bbc);
        });
    }

    public static void setViagraBoost(Player player, int boost) {
        PenisStatistics stats = getStatistics(player);
        stats.viagraBoost = boost;
        
        // Also update stored data
        PlayerData data = StorageManager.getPlayerData(player);
        data.viagraBoost = boost;
        
        if (stats.penisModel != null) {
            stats.penisModel.reload(stats);
        }
    }

    public static void setPenisSize(Player player, int size) {
        PenisStatistics stats = getStatistics(player);
        stats.size = size;
        
        // Update stored data
        PlayerData data = StorageManager.getPlayerData(player);
        data.penisSize = size;
        StorageManager.savePlayerData(player.getUniqueId());
        
        if (stats.penisModel != null) {
            stats.penisModel.reload(stats);
        }
    }

    public static void setPenisGirth(Player player, int girth) {
        PenisStatistics stats = getStatistics(player);
        stats.girth = girth;
        
        // Update stored data
        PlayerData data = StorageManager.getPlayerData(player);
        data.penisGirth = girth;
        StorageManager.savePlayerData(player.getUniqueId());
        
        if (stats.penisModel != null) {
            stats.penisModel.reload(stats);
        }
    }

    public static void setPenisBbc(Player player, boolean bbc) {
        PenisStatistics stats = getStatistics(player);
        stats.bbc = bbc;
        
        // Update stored data
        PlayerData data = StorageManager.getPlayerData(player);
        data.bbc = bbc;
        StorageManager.savePlayerData(player.getUniqueId());
        
        if (stats.penisModel != null) {
            stats.penisModel.reload(stats);
        }
    }

    public static void setActivePenis(Player player, PenisModel model, int taskId) {
        PenisStatistics stats = getStatistics(player);
        stats.penisModel = model;
        stats.runnableTaskId = taskId;
    }

    public static void clearActivePenis(Player player) {
        PenisStatistics stats = runtimeStats.get(player.getUniqueId());
        if (stats != null && stats.penisModel != null) {
            stats.penisModel.discard();
            Bukkit.getScheduler().cancelTask(stats.runnableTaskId);
            stats.penisModel = null;
            stats.runnableTaskId = 0;
        }
    }

    /**
     * Removes all GoonCraft block display entities from all worlds.
     * Called on startup and shutdown to prevent floating models.
     */
    public static void cleanupFloatingModels() {
        for (World world : Plugin.instance.getServer().getWorlds()) {
            world.getEntities().stream()
                .filter(e -> e.getType() == EntityType.BLOCK_DISPLAY)
                .filter(e -> "GOONCRAFT".equalsIgnoreCase(e.getName()))
                .forEach(org.bukkit.entity.Entity::remove);
        }
    }

    /**
     * Clears all active penis models for all online players.
     * Called on plugin disable.
     */
    public static void clearAllActivePenises() {
        for (Player player : Plugin.instance.getServer().getOnlinePlayers()) {
            clearActivePenis(player);
        }
        // Also clean up any orphaned entities
        cleanupFloatingModels();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        clearActivePenis(event.getPlayer());
        runtimeStats.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerSwing(PlayerAnimationEvent event) {
        if (event.getAnimationType() != PlayerAnimationType.ARM_SWING) {
            return;
        }
        
        Player player = event.getPlayer();
        if (player.getInventory().getItemInMainHand().getType() != Material.AIR) {
            return;
        }
        
        PenisStatistics stats = runtimeStats.get(player.getUniqueId());
        if (stats != null && stats.penisModel != null) {
            stats.penisModel.cum();
        }
    }
}
