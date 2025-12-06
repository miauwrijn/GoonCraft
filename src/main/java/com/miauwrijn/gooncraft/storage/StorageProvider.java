package com.miauwrijn.gooncraft.storage;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Abstract interface for player data storage.
 * Implementations can use file storage, MySQL, PostgreSQL, etc.
 */
public interface StorageProvider {

    /**
     * Initialize the storage provider (create tables, folders, etc.).
     * @return true if initialization was successful
     */
    boolean initialize();

    /**
     * Shutdown the storage provider (close connections, etc.).
     */
    void shutdown();

    /**
     * Check if the storage provider is connected and ready.
     */
    boolean isConnected();

    /**
     * Load player data from storage.
     * @param uuid Player's UUID
     * @return PlayerData or null if not found
     */
    PlayerData loadPlayerData(UUID uuid);

    /**
     * Load player data asynchronously.
     * @param uuid Player's UUID
     * @return CompletableFuture with PlayerData
     */
    CompletableFuture<PlayerData> loadPlayerDataAsync(UUID uuid);

    /**
     * Save player data to storage.
     * @param data Player data to save
     * @return true if save was successful
     */
    boolean savePlayerData(PlayerData data);

    /**
     * Save player data asynchronously.
     * @param data Player data to save
     * @return CompletableFuture with success status
     */
    CompletableFuture<Boolean> savePlayerDataAsync(PlayerData data);

    /**
     * Delete player data from storage.
     * @param uuid Player's UUID
     * @return true if deletion was successful
     */
    boolean deletePlayerData(UUID uuid);

    /**
     * Check if player data exists in storage.
     * @param uuid Player's UUID
     * @return true if data exists
     */
    boolean hasPlayerData(UUID uuid);

    /**
     * Get the name of this storage provider (for logging).
     */
    String getName();
}
