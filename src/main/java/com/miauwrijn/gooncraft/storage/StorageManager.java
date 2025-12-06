package com.miauwrijn.gooncraft.storage;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.miauwrijn.gooncraft.Plugin;
import com.miauwrijn.gooncraft.managers.RankPerkManager;
import com.miauwrijn.gooncraft.models.BoobModel;
import com.miauwrijn.gooncraft.models.PenisModel;
import com.miauwrijn.gooncraft.storage.DatabaseStorageProvider.DatabaseType;

/**
 * Central manager for player data storage.
 * Handles provider selection, caching, and player events.
 */
public class StorageManager implements Listener {

    private static StorageManager instance;
    private static StorageProvider provider;
    private static final Map<UUID, PlayerData> cache = new ConcurrentHashMap<>();

    public StorageManager() {
        instance = this;
        initializeProvider();
        
        // Register events
        Bukkit.getPluginManager().registerEvents(this, Plugin.instance);
        
        // Load data for online players (in case of reload)
        for (Player player : Bukkit.getOnlinePlayers()) {
            loadPlayer(player.getUniqueId());
        }
        
        // Auto-save every 5 minutes
        Bukkit.getScheduler().runTaskTimerAsynchronously(Plugin.instance, this::saveAll, 6000L, 6000L);
    }

    /**
     * Initialize the storage provider based on config.
     */
    private void initializeProvider() {
        FileConfiguration config = Plugin.instance.getConfig();
        
        String storageType = config.getString("storage.type", "file").toLowerCase();
        
        if (storageType.equals("mysql") || storageType.equals("postgresql")) {
            // Database storage
            DatabaseType dbType = storageType.equals("mysql") ? DatabaseType.MYSQL : DatabaseType.POSTGRESQL;
            String host = config.getString("storage.database.host", "localhost");
            int port = config.getInt("storage.database.port", dbType == DatabaseType.MYSQL ? 3306 : 5432);
            String database = config.getString("storage.database.database", "gooncraft");
            String username = config.getString("storage.database.username", "root");
            String password = config.getString("storage.database.password", "");
            String tablePrefix = config.getString("storage.database.table-prefix", "gooncraft_");
            
            provider = new DatabaseStorageProvider(dbType, host, port, database, username, password, tablePrefix);
            
            if (!provider.initialize()) {
                Plugin.instance.getLogger().warning("Failed to connect to database! Falling back to file storage.");
                provider = new FileStorageProvider();
                provider.initialize();
            }
        } else {
            // File storage (default)
            provider = new FileStorageProvider();
            provider.initialize();
        }
        
        Plugin.instance.getLogger().info("Using storage provider: " + provider.getName());
    }

    /**
     * Get the active storage provider.
     */
    public static StorageProvider getProvider() {
        return provider;
    }

    /**
     * Get cached player data, loading if necessary.
     */
    public static PlayerData getPlayerData(UUID uuid) {
        return cache.computeIfAbsent(uuid, k -> provider.loadPlayerData(uuid));
    }

    /**
     * Get cached player data for a player.
     */
    public static PlayerData getPlayerData(Player player) {
        return getPlayerData(player.getUniqueId());
    }

    /**
     * Update cached player data.
     */
    public static void updatePlayerData(UUID uuid, PlayerData data) {
        cache.put(uuid, data);
    }

    /**
     * Save player data (sync to storage).
     */
    public static void savePlayerData(UUID uuid) {
        PlayerData data = cache.get(uuid);
        if (data != null) {
            provider.savePlayerDataAsync(data);
        }
    }

    /**
     * Save player data immediately (blocking).
     */
    public static void savePlayerDataSync(UUID uuid) {
        PlayerData data = cache.get(uuid);
        if (data != null) {
            provider.savePlayerData(data);
        }
    }

    /**
     * Load player data into cache.
     */
    public static void loadPlayer(UUID uuid) {
        PlayerData data = provider.loadPlayerData(uuid);
        cache.put(uuid, data);
    }

    /**
     * Unload player data (save and remove from cache).
     */
    public static void unloadPlayer(UUID uuid) {
        PlayerData data = cache.remove(uuid);
        if (data != null) {
            provider.savePlayerData(data);
        }
    }

    /**
     * Save all cached player data.
     */
    public void saveAll() {
        for (Map.Entry<UUID, PlayerData> entry : cache.entrySet()) {
            provider.savePlayerDataAsync(entry.getValue());
        }
    }

    /**
     * Save all and shutdown.
     */
    public void shutdown() {
        // Save all data synchronously before shutdown
        for (Map.Entry<UUID, PlayerData> entry : cache.entrySet()) {
            provider.savePlayerData(entry.getValue());
        }
        cache.clear();
        
        if (provider != null) {
            provider.shutdown();
        }
    }

    /**
     * Reload storage configuration.
     */
    public void reload() {
        // Save current data
        saveAll();
        
        // Shutdown current provider
        if (provider != null) {
            provider.shutdown();
        }
        
        // Reinitialize with new config
        initializeProvider();
        
        // Reload cached data
        for (UUID uuid : cache.keySet()) {
            cache.put(uuid, provider.loadPlayerData(uuid));
        }
    }

    // ===== Event Handlers =====

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Load async to not block join
        provider.loadPlayerDataAsync(player.getUniqueId()).thenAccept(data -> {
            // Initialize genital sizes if they're 0 (uninitialized)
            boolean needsSave = false;
            if (data.penisSize <= 0) {
                data.penisSize = PenisModel.getRandomSize();
                needsSave = true;
            }
            if (data.penisGirth <= 0) {
                data.penisGirth = PenisModel.getRandomGirth();
                needsSave = true;
            }
            if (data.boobSize <= 0) {
                data.boobSize = BoobModel.getRandomSize();
                needsSave = true;
            }
            if (data.boobPerkiness <= 0) {
                data.boobPerkiness = BoobModel.getRandomPerkiness();
                needsSave = true;
            }
            
            cache.put(player.getUniqueId(), data);
            
            // Save if we initialized any values
            if (needsSave) {
                provider.savePlayerDataAsync(data);
            }
            
            // Initialize rank perks after data is loaded
            Bukkit.getScheduler().runTaskLater(Plugin.instance, () -> {
                if (player.isOnline()) {
                    RankPerkManager.initializePlayer(player);
                }
            }, 60L); // 3 second delay to ensure everything is loaded
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        // Clean up rank perk manager
        RankPerkManager.cleanup(player);
        
        // Save async and remove from cache
        PlayerData data = cache.remove(uuid);
        if (data != null) {
            provider.savePlayerDataAsync(data);
        }
    }

    public static StorageManager getInstance() {
        return instance;
    }
}
