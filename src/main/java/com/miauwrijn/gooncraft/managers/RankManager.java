package com.miauwrijn.gooncraft.managers;

import java.util.List;

import org.bukkit.entity.Player;

import com.miauwrijn.gooncraft.data.PlayerStats;
import com.miauwrijn.gooncraft.ranks.BaseRank;
import com.miauwrijn.gooncraft.ranks.RankBuilder;

/**
 * Manages player ranks based on XP (experience points).
 * XP is earned through actions (gooning, bodily functions) and achievements.
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
     * Gets the rank for a player based on their XP.
     */
    public static BaseRank getRank(Player player) {
        long xp = getPlayerXp(player);
        return getRankForXp(xp);
    }
    
    /**
     * Get the player's current XP.
     */
    public static long getPlayerXp(Player player) {
        PlayerStats stats = StatisticsManager.getStats(player);
        return stats != null ? stats.experience : 0;
    }

    /**
     * Gets the rank for a specific XP amount.
     */
    public static BaseRank getRankForXp(long xp) {
        BaseRank currentRank = RANKS[0]; // Innocent
        
        for (BaseRank rank : RANKS) {
            if (xp >= rank.getRequiredXp()) {
                currentRank = rank;
            } else {
                break;
            }
        }
        
        return currentRank;
    }
    
    /** @deprecated Use getRankForXp() instead */
    @Deprecated
    public static BaseRank getRankForAchievements(int achievementCount) {
        return getRankForXp(achievementCount);
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
        long xp = getPlayerXp(player);
        BaseRank currentRank = getRankForXp(xp);
        BaseRank nextRank = getNextRank(currentRank);
        
        if (nextRank == null) {
            return 1.0; // Max rank
        }
        
        long currentThreshold = currentRank.getRequiredXp();
        long nextThreshold = nextRank.getRequiredXp();
        long range = nextThreshold - currentThreshold;
        long progress = xp - currentThreshold;
        
        if (range <= 0) return 1.0;
        return (double) progress / range;
    }

    /**
     * Gets XP needed for next rank.
     */
    public static long getXpToNextRank(Player player) {
        long xp = getPlayerXp(player);
        BaseRank currentRank = getRankForXp(xp);
        BaseRank nextRank = getNextRank(currentRank);
        
        if (nextRank == null) {
            return 0; // Max rank
        }
        
        return nextRank.getRequiredXp() - xp;
    }
    
    /** @deprecated Use getXpToNextRank() instead */
    @Deprecated
    public static int getAchievementsToNextRank(Player player) {
        return (int) getXpToNextRank(player);
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
    
    /**
     * Creates a colored progress bar based on progress percentage.
     */
    public static String createColoredProgressBar(double progress, int length) {
        int filled = (int) (progress * length);
        StringBuilder bar = new StringBuilder("§8[");
        
        for (int i = 0; i < length; i++) {
            if (i < filled) {
                // Color gradient: red -> yellow -> green
                if (progress < 0.33) {
                    bar.append("§c█");
                } else if (progress < 0.66) {
                    bar.append("§e█");
                } else {
                    bar.append("§a█");
                }
            } else {
                bar.append("§7░");
            }
        }
        
        bar.append("§8]");
        return bar.toString();
    }
}
