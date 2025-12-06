package com.miauwrijn.gooncraft.managers;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

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
    
    // Cooldown for genital bump kicks (player UUID pair -> last kick time)
    private static final Map<String, Long> bumpCooldowns = new ConcurrentHashMap<>();
    private static final long BUMP_COOLDOWN_MS = 1500; // 1.5 second cooldown between kicks

    public PenisStatisticManager() {
        // Clean up any floating models from previous session
        cleanupFloatingModels();
        
        Bukkit.getPluginManager().registerEvents(this, Plugin.instance);
        
        // Start the genital bump collision checker
        startBumpCollisionChecker();
    }
    
    /**
     * Starts a task that checks for genital bump collisions.
     * If a player has genitals out and bumps into another player while facing them,
     * the other player gets kicked based on genital size.
     */
    private void startBumpCollisionChecker() {
        new BukkitRunnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                
                // Clean up old cooldowns
                bumpCooldowns.entrySet().removeIf(e -> now - e.getValue() > BUMP_COOLDOWN_MS * 2);
                
                for (Player player : Bukkit.getOnlinePlayers()) {
                    // Check if player has genitals out
                    int genitalSize = getActiveGenitalSize(player);
                    if (genitalSize <= 0) continue;
                    
                    Location playerLoc = player.getLocation();
                    Vector playerDirection = playerLoc.getDirection().setY(0).normalize();
                    
                    // Check nearby players
                    for (Player target : player.getWorld().getPlayers()) {
                        if (target.equals(player)) continue;
                        
                        Location targetLoc = target.getLocation();
                        double distance = playerLoc.distance(targetLoc);
                        
                        // Must be very close (within 1.5 blocks)
                        if (distance > 1.5 || distance < 0.3) continue;
                        
                        // Check if player is facing the target
                        Vector toTarget = targetLoc.toVector().subtract(playerLoc.toVector()).setY(0).normalize();
                        double dot = playerDirection.dot(toTarget);
                        
                        // Must be facing them (dot > 0.7 means within ~45 degrees)
                        if (dot < 0.7) continue;
                        
                        // Check cooldown
                        String cooldownKey = player.getUniqueId() + "-" + target.getUniqueId();
                        Long lastKick = bumpCooldowns.get(cooldownKey);
                        if (lastKick != null && now - lastKick < BUMP_COOLDOWN_MS) continue;
                        
                        // Apply the kick!
                        applyGenitalBump(player, target, genitalSize, toTarget);
                        bumpCooldowns.put(cooldownKey, now);
                    }
                }
            }
        }.runTaskTimer(Plugin.instance, 5L, 5L); // Check every 5 ticks (0.25 seconds)
    }
    
    /**
     * Get the size of active genitals (penis size or vagina equivalent).
     * Returns 0 if no genitals are out.
     */
    private static int getActiveGenitalSize(Player player) {
        // Check penis
        PenisStatistics stats = runtimeStats.get(player.getUniqueId());
        if (stats != null && stats.penisModel != null) {
            return stats.size + stats.viagraBoost + stats.rankSizeBoost;
        }
        
        // Check vagina (use a base "size" for vagina - we'll use arousal/wetness equivalent)
        if (GenderManager.getActiveVaginaModel(player) != null) {
            // Vagina bump power based on boob size (if they have boobs) as a proxy
            PlayerData data = StorageManager.getPlayerData(player);
            return Math.max(10, data.boobSize / 2); // Min 10, scales with boob size
        }
        
        return 0;
    }
    
    /**
     * Apply the genital bump kick to a target player.
     */
    private static void applyGenitalBump(Player bumper, Player target, int genitalSize, Vector direction) {
        // Calculate kick strength based on genital size
        // Size ranges from ~5 to ~30, we want velocity from 0.3 to 1.0
        double strength = 0.3 + (genitalSize / 40.0) * 0.7;
        strength = Math.min(1.2, strength); // Cap at 1.2
        
        // Add slight upward component
        Vector kickVelocity = direction.clone().multiply(strength);
        kickVelocity.setY(0.2 + (genitalSize / 100.0)); // Small upward boost
        
        // Apply velocity
        target.setVelocity(target.getVelocity().add(kickVelocity));
        
        // Play a bonk sound
        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 0.8f, 0.8f);
        
        // Send messages
        String bumperName = bumper.getName();
        PenisStatistics stats = runtimeStats.get(bumper.getUniqueId());
        boolean isPenis = stats != null && stats.penisModel != null;
        
        if (isPenis) {
            target.sendMessage("§d" + bumperName + " §7bonked you with their §dcock§7!");
        } else {
            target.sendMessage("§d" + bumperName + " §7hip-checked you with their §dpussy§7!");
        }
    }

    /**
     * Get runtime statistics (including active model) for a player.
     * Creates one if it doesn't exist.
     */
    public static PenisStatistics getStatistics(Player player) {
        return runtimeStats.computeIfAbsent(player.getUniqueId(), k -> {
            PlayerData data = StorageManager.getPlayerData(player);
            // Ensure we have valid sizes (0 = uninitialized)
            int size = data.penisSize > 0 ? data.penisSize : PenisModel.getRandomSize();
            int girth = data.penisGirth > 0 ? data.penisGirth : PenisModel.getRandomGirth();
            
            // Update stored data if we initialized
            if (data.penisSize <= 0 || data.penisGirth <= 0) {
                data.penisSize = size;
                data.penisGirth = girth;
                StorageManager.savePlayerData(player.getUniqueId());
            }
            
            return new PenisStatistics(size, girth, data.bbc);
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
     * Update rank boosts and reload model if active.
     */
    public static void updateRankBoosts(Player player, int sizeBoost, int girthBoost) {
        PenisStatistics stats = getStatistics(player);
        stats.rankSizeBoost = sizeBoost;
        stats.rankGirthBoost = girthBoost;
        
        // Update model if active
        if (stats.penisModel != null) {
            stats.penisModel.reload(stats);
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
        
        // Check for penis model
        PenisStatistics stats = runtimeStats.get(player.getUniqueId());
        if (stats != null && stats.penisModel != null) {
            stats.penisModel.cum();
            return; // Only one genital can goon at a time
        }
        
        // Check for vagina model
        com.miauwrijn.gooncraft.models.VaginaModel vaginaModel = GenderManager.getActiveVaginaModel(player);
        if (vaginaModel != null) {
            vaginaModel.goon();
        }
    }
}
