package com.miauwrijn.gooncraft.managers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.miauwrijn.gooncraft.Plugin;
import com.miauwrijn.gooncraft.achievements.AchievementBuilder;
import com.miauwrijn.gooncraft.achievements.BaseAchievement;
import com.miauwrijn.gooncraft.achievements.LocationAchievement;
import com.miauwrijn.gooncraft.achievements.MobProximityAchievement;
import com.miauwrijn.gooncraft.achievements.StatAchievement;
import com.miauwrijn.gooncraft.data.PlayerStats;
import com.miauwrijn.gooncraft.storage.PlayerData;
import com.miauwrijn.gooncraft.storage.StorageManager;

/**
 * Manages achievements for players.
 * Uses StorageManager for persistence (supports file and database storage).
 * All achievements are loaded from achievements.yml - no enum needed.
 */
public class AchievementManager {

    public AchievementManager() {
        // No initialization needed - StorageManager handles data
    }

    /**
     * Get unlocked achievements for a player from StorageManager.
     */
    public static Set<String> getUnlocked(Player player) {
        PlayerData data = StorageManager.getPlayerData(player);
        if (data.unlockedAchievements == null) {
            data.unlockedAchievements = new HashSet<>();
        }
        return data.unlockedAchievements;
    }

    public static void checkAchievements(Player player, PlayerStats stats) {
        Set<String> unlocked = getUnlocked(player);
        
        // Get all achievements from YAML
        Map<String, BaseAchievement> allAchievements = AchievementBuilder.getAllAchievementsById();
        
        for (BaseAchievement achievement : allAchievements.values()) {
            String achievementId = achievement.getId().toLowerCase();
            
            // Skip if already unlocked
            if (unlocked.contains(achievementId)) continue;
            
            // Skip location-based and mob proximity achievements - they're handled separately
            if (achievement instanceof LocationAchievement || achievement instanceof MobProximityAchievement) {
                continue;
            }
            
            // Only check stat-based achievements here
            if (achievement instanceof StatAchievement statAchievement) {
                long currentValue = getStatForCategory(stats, statAchievement.getStatCategory());
                
                if (currentValue >= achievement.getThreshold()) {
                    unlockAchievementById(player, achievementId, achievement);
                }
            }
        }
    }

    private static long getStatForCategory(PlayerStats stats, String category) {
        return switch (category) {
            case "goon" -> stats.goonCount;
            case "cum_on" -> stats.cumOnOthersCount;
            case "got_cummed" -> stats.gotCummedOnCount;
            case "time_out" -> stats.getCurrentTotalTime();
            case "bf_given" -> stats.buttfingersGiven;
            case "bf_received" -> stats.buttfingersReceived;
            case "viagra" -> stats.viagraUsed;
            case "fart" -> stats.fartCount;
            case "poop" -> stats.poopCount;
            case "piss" -> stats.pissCount;
            case "jiggle" -> stats.jiggleCount;
            case "boob_toggle" -> stats.boobToggleCount;
            case "gender_changes" -> stats.genderChanges;
            case "gender_other" -> stats.genderChanges > 0 ? 1 : 0; // Handled specially
            case "unique_cum" -> stats.uniquePlayersCummedOn.size();
            case "unique_got_cum" -> stats.uniquePlayersGotCummedBy.size();
            case "unique_piss" -> stats.uniquePlayersPissedNear.size();
            case "unique_fart" -> stats.uniquePlayersFartedNear.size();
            case "unique_bf" -> stats.uniquePlayersButtfingered.size();
            case "damage_goon" -> stats.damageWhileGooning;
            case "death_exposed" -> stats.deathsWhileExposed;
            case "goon_fire" -> stats.goonsWhileOnFire;
            case "goon_falling" -> stats.goonsWhileFalling;
            case "creeper_death" -> stats.creeperDeathsWhileExposed;
            case "speed_goon" -> stats.maxGoonsInMinute;
            case "rapid_fire" -> stats.ejaculationsIn30Seconds;
            default -> 0;
        };
    }

    /**
     * Check location-based achievements.
     */
    public static void checkLocationAchievements(Player player, PlayerStats stats) {
        Set<String> unlocked = getUnlocked(player);
        
        // Get all location achievements from YAML
        Map<String, BaseAchievement> allAchievements = AchievementBuilder.getAllAchievementsById();
        
        for (BaseAchievement achievement : allAchievements.values()) {
            if (!(achievement instanceof LocationAchievement locationAchievement)) {
                continue;
            }
            
            String achievementId = achievement.getId().toLowerCase();
            if (unlocked.contains(achievementId)) {
                continue;
            }
            
            String locationTag = locationAchievement.getLocationTag().toLowerCase();
            boolean conditionMet = switch (locationTag) {
                case "nether" -> stats.goonedInNether;
                case "end" -> stats.goonedInEnd;
                case "underwater" -> stats.goonedUnderwater;
                case "desert" -> stats.goonedInDesert;
                case "snow" -> stats.goonedInSnow;
                case "high" -> stats.goonedHighAltitude;
                default -> false;
            };
            
            if (conditionMet) {
                unlockAchievementById(player, achievementId, achievement);
            }
        }
    }

    /**
     * Unlock an achievement by ID.
     */
    private static void unlockAchievementById(Player player, String achievementId, BaseAchievement achievement) {
        Set<String> unlocked = getUnlocked(player);
        if (unlocked.contains(achievementId)) return; // Double check
        
        unlocked.add(achievementId);
        
        // Notify player
        player.sendMessage("");
        player.sendMessage(ConfigManager.getMessage("achievement.unlocked-title"));
        player.sendMessage(ConfigManager.getMessage("achievement.unlocked-name", "{name}", achievement.getName()));
        player.sendMessage(ConfigManager.getMessage("achievement.unlocked-description", "{description}", achievement.getDescription()));
        player.sendMessage("");
        
        // Sound effect
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        
        // Broadcast to server
        String broadcast = ConfigManager.getMessage("achievement.broadcast",
            "{player}", player.getName(),
            "{name}", achievement.getName());
        Bukkit.broadcastMessage(broadcast);
        
        // Save immediately via StorageManager
        StorageManager.savePlayerData(player.getUniqueId());
        
        // Check for rank up
        checkRankUp(player);
    }
    
    /**
     * Check if player ranked up.
     */
    private static void checkRankUp(Player player) {
        com.miauwrijn.gooncraft.ranks.BaseRank oldRank = RankManager.getRankForAchievements(getUnlockedCount(player) - 1);
        com.miauwrijn.gooncraft.ranks.BaseRank newRank = RankManager.getRank(player);
        
        if (oldRank != newRank) {
            // Player ranked up!
            // Apply rank perks (they'll be applied by applyAllRankPerks which checks up to current rank)
            RankPerkManager.applyAllRankPerks(player);
            
            // Notify about rank up
            player.sendMessage("");
            player.sendMessage("§6§l✨ RANK UP! ✨");
            player.sendMessage("§7You are now: " + newRank.getDisplayName());
            if (!newRank.getDescription().isEmpty()) {
                player.sendMessage("§8" + newRank.getDescription());
            }
            if (!newRank.getPerkDescriptions().isEmpty()) {
                player.sendMessage("§aPerks:");
                for (String perk : newRank.getPerkDescriptions()) {
                    player.sendMessage("§a  • " + perk);
                }
            }
            player.sendMessage("");
        }
    }

    public static int getUnlockedCount(Player player) {
        return getUnlocked(player).size();
    }

    public static int getTotalAchievements() {
        return AchievementBuilder.getAllAchievementsById().size();
    }

    /**
     * Get total non-hidden achievements.
     */
    public static int getVisibleAchievements() {
        int count = 0;
        for (BaseAchievement achievement : AchievementBuilder.getAllAchievementsById().values()) {
            if (!achievement.isHidden()) count++;
        }
        return count;
    }

    /**
     * Manually unlock an achievement by ID (for easter eggs).
     * Returns true if newly unlocked, false if already had it.
     */
    public static boolean tryUnlockById(Player player, String achievementId) {
        Set<String> unlocked = getUnlocked(player);
        String normalizedId = achievementId.toLowerCase();
        
        if (unlocked.contains(normalizedId)) {
            return false;
        }
        
        // Get achievement from YAML
        BaseAchievement achievement = AchievementBuilder.getAchievementById(normalizedId);
        
        if (achievement != null) {
            unlockAchievementById(player, normalizedId, achievement);
            return true;
        }
        return false;
    }

    /**
     * Check if an achievement is hidden (shows as ??? until unlocked).
     */
    public static boolean isHidden(String achievementId) {
        BaseAchievement achievement = AchievementBuilder.getAchievementById(achievementId.toLowerCase());
        return achievement != null && achievement.isHidden();
    }

    /**
     * Get all achievements by category for display.
     * If category is null, returns all achievements.
     */
    public static List<BaseAchievement> getAchievementsByCategory(String category) {
        Map<String, BaseAchievement> allAchievements = AchievementBuilder.getAllAchievementsById();
        
        // If no achievements loaded, try loading them now (fallback)
        if (allAchievements.isEmpty()) {
            Plugin.instance.getLogger().warning("No achievements loaded! Attempting to load now...");
            AchievementBuilder.loadAchievements();
            allAchievements = AchievementBuilder.getAllAchievementsById();
        }
        
        if (category == null) {
            return new ArrayList<>(allAchievements.values());
        }
        return allAchievements.values().stream()
            .filter(a -> a.getCategory().equals(category) || 
                        (category.equals("hidden") && a.isHidden()))
            .toList();
    }

    /**
     * Get an achievement by ID.
     */
    public static BaseAchievement getAchievement(String achievementId) {
        return AchievementBuilder.getAchievementById(achievementId.toLowerCase());
    }

    /**
     * Check for mob proximity achievements when player is gooning.
     * Checks all nearby entities and unlocks achievements if matching mob types are found.
     */
    public static void checkMobProximityAchievements(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }
        
        // Get nearby entities (within 10 blocks)
        List<Entity> nearbyEntities = player.getNearbyEntities(10, 10, 10);
        
        // Build a set of nearby mob types (normalized to lowercase)
        Set<String> nearbyMobTypes = new HashSet<>();
        for (Entity entity : nearbyEntities) {
            if (entity instanceof LivingEntity) {
                EntityType type = entity.getType();
                // Convert EntityType to lowercase string (e.g., ZOMBIE -> "zombie")
                String mobType = type.name().toLowerCase();
                nearbyMobTypes.add(mobType);
            }
        }
        
        if (nearbyMobTypes.isEmpty()) {
            return; // No nearby mobs
        }
        
        // Get all achievements loaded from YAML
        Map<String, BaseAchievement> allAchievements = AchievementBuilder.getAllAchievementsById();
        
        // Check each mob proximity achievement
        for (BaseAchievement achievement : allAchievements.values()) {
            if (!(achievement instanceof MobProximityAchievement mobAchievement)) {
                continue;
            }
            
            String requiredMobType = mobAchievement.getMobType().toLowerCase();
            
            // Check if player is near the required mob type
            if (nearbyMobTypes.contains(requiredMobType)) {
                String achievementId = achievement.getId().toLowerCase();
                
                // Check if already unlocked
                Set<String> unlocked = getUnlocked(player);
                if (!unlocked.contains(achievementId)) {
                    unlockAchievementById(player, achievementId, achievement);
                }
            }
        }
    }

}
