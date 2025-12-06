package com.miauwrijn.gooncraft.managers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cat;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.miauwrijn.gooncraft.Plugin;
import com.miauwrijn.gooncraft.data.PenisStatistics;
import com.miauwrijn.gooncraft.managers.GenderManager.Gender;
import com.miauwrijn.gooncraft.perks.BasePerk;
import com.miauwrijn.gooncraft.perks.CooldownReductionPerk;
import com.miauwrijn.gooncraft.perks.FapSpeedPerk;
import com.miauwrijn.gooncraft.ranks.BaseRank;

/**
 * Manages rank perks - applies and removes temporary stat boosts.
 * Perks are NOT permanent - they're applied on join and can be toggled.
 */
public class RankPerkManager {
    
    // Track which ranks have perks currently active
    private static final Map<UUID, Set<BaseRank>> activeRankPerks = new HashMap<>();
    
    // Track following animals per player
    private static final Map<UUID, Set<LivingEntity>> followingAnimals = new HashMap<>();
    
    /**
     * Apply all rank perks up to the player's current rank (on join/rank up).
     */
    public static void applyAllRankPerks(Player player) {
        BaseRank currentRank = RankManager.getRank(player);
        BaseRank[] allRanks = RankManager.getAllRanks();
        
        Set<BaseRank> active = activeRankPerks.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>());
        
        // Apply all ranks up to current
        for (BaseRank rank : allRanks) {
            if (rank.getOrdinal() <= currentRank.getOrdinal() && !active.contains(rank)) {
                rank.applyPerks(player);
                active.add(rank);
            }
        }
        
        // Start animal following task if applicable
        startAnimalFollowingTask(player);
    }
    
    /**
     * Remove all rank perks (when player quits or resets).
     */
    public static void removeAllRankPerks(Player player) {
        Set<BaseRank> active = activeRankPerks.get(player.getUniqueId());
        if (active == null) return;
        
        // Remove in reverse order
        BaseRank[] allRanks = RankManager.getAllRanks();
        for (int i = allRanks.length - 1; i >= 0; i--) {
            BaseRank rank = allRanks[i];
            if (active.contains(rank)) {
                rank.removePerks(player);
            }
        }
        
        active.clear();
        stopAnimalFollowing(player);
    }
    
    /**
     * Toggle a specific rank's perks on/off.
     */
    public static void toggleRankPerks(Player player, BaseRank rank, boolean enabled) {
        Set<BaseRank> active = activeRankPerks.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>());
        
        if (enabled && !active.contains(rank)) {
            rank.applyPerks(player);
            active.add(rank);
        } else if (!enabled && active.contains(rank)) {
            rank.removePerks(player);
            active.remove(rank);
        }
    }
    
    /**
     * Check if a rank's perks are currently active for a player.
     */
    public static boolean hasRankPerksActive(Player player, BaseRank rank) {
        Set<BaseRank> active = activeRankPerks.get(player.getUniqueId());
        return active != null && active.contains(rank);
    }
    
    /**
     * Get permanent cooldown reduction from active rank perks.
     * Queries CooldownReductionPerk objects by type.
     */
    public static double getCooldownReduction(Player player) {
        double reduction = 0.0;
        Set<BaseRank> active = activeRankPerks.get(player.getUniqueId());
        if (active == null) return 0.0;
        
        for (BaseRank rank : active) {
            for (BasePerk perk : rank.getPerks()) {
                if (perk instanceof CooldownReductionPerk) {
                    reduction = Math.max(reduction, perk.getCooldownReduction());
                }
            }
        }
        
        return Math.min(0.70, reduction); // Cap at 70%
    }
    
    /**
     * Get permanent fap speed multiplier from active rank perks.
     * Queries FapSpeedPerk objects by type.
     */
    public static double getFapSpeedMultiplier(Player player) {
        double multiplier = 1.0;
        Set<BaseRank> active = activeRankPerks.get(player.getUniqueId());
        if (active == null) return 1.0;
        
        for (BaseRank rank : active) {
            for (BasePerk perk : rank.getPerks()) {
                if (perk instanceof FapSpeedPerk) {
                    multiplier += (perk.getFapSpeedMultiplier() - 1.0);
                }
            }
        }
        
        return Math.min(2.0, multiplier); // Cap at 2x speed
    }
    
    /**
     * Start animal following if player has the perk and genitals are out.
     * Queries animal following perks by type.
     */
    public static void checkAnimalFollowing(Player player) {
        Set<BaseRank> active = activeRankPerks.get(player.getUniqueId());
        if (active == null) {
            stopAnimalFollowing(player);
            return;
        }
        
        // Check for animal following perks
        for (BaseRank rank : active) {
            for (BasePerk perk : rank.getPerks()) {
                if (perk.isAnimalFollowingPerk()) {
                    String animalType = perk.getAnimalType();
                    if ("cockmaster".equals(animalType)) {
                        Gender gender = GenderManager.getGender(player);
                        if ((gender == Gender.MALE || gender == Gender.OTHER)) {
                            PenisStatistics stats = PenisStatisticManager.getStatistics(player);
                            if (stats != null && stats.penisModel != null) {
                                startChickenFollowing(player);
                                return;
                            }
                        }
                    } else if ("pussy_magnet".equals(animalType)) {
                        Gender gender = GenderManager.getGender(player);
                        if (gender == Gender.FEMALE || (gender == Gender.OTHER && GenderManager.getActiveVaginaModel(player) != null)) {
                            startCatFollowing(player);
                            return;
                        }
                    }
                }
            }
        }
        
        // Stop following if genitals are hidden
        stopAnimalFollowing(player);
    }
    
    /**
     * Start chickens following the player's cock.
     */
    private static void startChickenFollowing(Player player) {
        UUID uuid = player.getUniqueId();
        Set<LivingEntity> animals = followingAnimals.computeIfAbsent(uuid, k -> new HashSet<>());
        
        // Remove invalid chickens
        animals.removeIf(entity -> !(entity instanceof Chicken) || entity.isDead() || !entity.isValid());
        
        // Find nearby chickens (within 32 blocks)
        player.getWorld().getNearbyEntities(player.getLocation(), 32, 32, 32, entity -> 
            entity instanceof Chicken && !animals.contains(entity)
        ).forEach(entity -> {
            Chicken chicken = (Chicken) entity;
            animals.add(chicken);
        });
        
        // Make chickens follow
        animals.stream()
            .filter(entity -> entity instanceof Chicken)
            .forEach(entity -> {
                Chicken chicken = (Chicken) entity;
                chicken.setTarget(player);
            });
    }
    
    /**
     * Start cats following the player's pussy.
     */
    private static void startCatFollowing(Player player) {
        UUID uuid = player.getUniqueId();
        Set<LivingEntity> animals = followingAnimals.computeIfAbsent(uuid, k -> new HashSet<>());
        
        // Remove invalid cats
        animals.removeIf(entity -> !(entity instanceof Cat) || entity.isDead() || !entity.isValid());
        
        // Find nearby cats (within 32 blocks)
        player.getWorld().getNearbyEntities(player.getLocation(), 32, 32, 32, entity -> 
            entity instanceof Cat && !animals.contains(entity)
        ).forEach(entity -> {
            Cat cat = (Cat) entity;
            animals.add(cat);
        });
        
        // Make cats follow
        animals.stream()
            .filter(entity -> entity instanceof Cat)
            .forEach(entity -> {
                Cat cat = (Cat) entity;
                cat.setTarget(player);
            });
    }
    
    /**
     * Stop animals from following the player.
     */
    public static void stopAnimalFollowing(Player player) {
        UUID uuid = player.getUniqueId();
        Set<LivingEntity> animals = followingAnimals.get(uuid);
        if (animals != null) {
            animals.forEach(entity -> {
                if (entity instanceof Mob mob) {
                    mob.setTarget(null);
                }
            });
            animals.clear();
        }
    }
    
    /**
     * Initialize rank perks for a player (on join).
     */
    public static void initializePlayer(Player player) {
        applyAllRankPerks(player);
    }
    
    /**
     * Start a task to periodically check for animal following.
     */
    private static void startAnimalFollowingTask(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    stopAnimalFollowing(player);
                    return;
                }
                
                checkAnimalFollowing(player);
            }
        }.runTaskTimer(Plugin.instance, 20L, 20L); // Check every second
    }
    
    /**
     * Clean up when player disconnects.
     */
    public static void cleanup(Player player) {
        removeAllRankPerks(player);
        followingAnimals.remove(player.getUniqueId());
        activeRankPerks.remove(player.getUniqueId());
    }
}
