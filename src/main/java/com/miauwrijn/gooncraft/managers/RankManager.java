package com.miauwrijn.gooncraft.managers;

import java.util.List;

import org.bukkit.entity.Player;

import com.miauwrijn.gooncraft.ranks.BaseRank;
import com.miauwrijn.gooncraft.ranks.RankBuilder;

/**
 * Manages player ranks based on achievement count.
 * Each achievement = 1 level toward the next rank.
 * 
 * Total achievements: 67
 * - Regular: 56
 * - Hidden: 11
 */
public class RankManager {

    // Array of all ranks in order (loaded from YAML)
    private static final BaseRank[] RANKS = initializeRanks();

    /**
     * Initialize all rank instances from YAML configuration.
     */
    private static BaseRank[] initializeRanks() {
        List<BaseRank> ranks = RankBuilder.loadRanks();
        return ranks.toArray(new BaseRank[0]);
    }

    /**
     * Gets the rank for a player based on their achievement count.
     */
    public static BaseRank getRank(Player player) {
        int achievementCount = AchievementManager.getUnlockedCount(player);
        return getRankForAchievements(achievementCount);
    }

    /**
     * Gets the rank for a specific achievement count.
     */
    public static BaseRank getRankForAchievements(int achievementCount) {
        BaseRank currentRank = RANKS[0]; // Innocent
        
        for (BaseRank rank : RANKS) {
            if (achievementCount >= rank.getRequiredAchievements()) {
                currentRank = rank;
            } else {
                break;
            }
        }
        
        return currentRank;
    }
    
    /**
     * Get all ranks.
     */
    public static BaseRank[] getAllRanks() {
        return RANKS.clone();
    }
    
    /**
     * Get rank by ordinal/index.
     */
    public static BaseRank getRankByOrdinal(int ordinal) {
        if (ordinal < 0 || ordinal >= RANKS.length) {
            return RANKS[0];
        }
        return RANKS[ordinal];
    }

    /**
     * Gets the next rank after the current one, or null if max rank.
     */
    public static BaseRank getNextRank(BaseRank currentRank) {
        int currentIndex = currentRank.getOrdinal();
        
        if (currentIndex + 1 < RANKS.length) {
            return RANKS[currentIndex + 1];
        }
        return null;
    }

    /**
     * Gets progress toward the next rank (0.0 to 1.0).
     */
    public static double getProgressToNextRank(Player player) {
        int achievements = AchievementManager.getUnlockedCount(player);
        BaseRank currentRank = getRankForAchievements(achievements);
        BaseRank nextRank = getNextRank(currentRank);
        
        if (nextRank == null) {
            return 1.0; // Max rank
        }
        
        int currentThreshold = currentRank.getRequiredAchievements();
        int nextThreshold = nextRank.getRequiredAchievements();
        int range = nextThreshold - currentThreshold;
        int progress = achievements - currentThreshold;
        
        return (double) progress / range;
    }

    /**
     * Gets achievements needed for next rank.
     */
    public static int getAchievementsToNextRank(Player player) {
        int achievements = AchievementManager.getUnlockedCount(player);
        BaseRank currentRank = getRankForAchievements(achievements);
        BaseRank nextRank = getNextRank(currentRank);
        
        if (nextRank == null) {
            return 0; // Max rank
        }
        
        return nextRank.getRequiredAchievements() - achievements;
    }

    /**
     * Creates a progress bar for rank progress.
     */
    public static String createProgressBar(double progress, int length) {
        int filled = (int) (progress * length);
        StringBuilder bar = new StringBuilder("§7[");
        
        for (int i = 0; i < length; i++) {
            if (i < filled) {
                bar.append("§a■");
            } else {
                bar.append("§8■");
            }
        }
        
        bar.append("§7]");
        return bar.toString();
    }
}
