package com.miauwrijn.gooncraft.managers;

import org.bukkit.entity.Player;

/**
 * Manages player ranks based on achievement count.
 * Each achievement = 1 level toward the next rank.
 */
public class RankManager {

    /**
     * Rank definitions with funny names.
     * Each rank requires a minimum number of achievements.
     */
    public enum Rank {
        INNOCENT(0, "§7Innocent Virgin", "§7", "👶"),
        CURIOUS(1, "§aCurious Toucher", "§a", "🤔"),
        AMATEUR(3, "§eAmateur Stroker", "§e", "✋"),
        ENTHUSIAST(5, "§6Goon Enthusiast", "§6", "🔥"),
        DEDICATED(8, "§cDedicated Degenerate", "§c", "💦"),
        ADVANCED(11, "§dAdvanced Coomer", "§d", "🍆"),
        PROFESSIONAL(14, "§5Professional Gooner", "§5", "👑"),
        EXPERT(17, "§bMaster Bater", "§b", "🎓"),
        ELITE(20, "§3Elite Exhibitionist", "§3", "⭐"),
        LEGENDARY(23, "§4Legendary Pervert", "§4", "🏆"),
        GOLDEN(26, "§6§lGolden Gooner", "§6§l", "✨"),
        ULTIMATE(27, "§d§l✦ ULTIMATE DEGENERATE ✦", "§d§l", "🌟");

        public final int requiredAchievements;
        public final String displayName;
        public final String color;
        public final String icon;

        Rank(int requiredAchievements, String displayName, String color, String icon) {
            this.requiredAchievements = requiredAchievements;
            this.displayName = displayName;
            this.color = color;
            this.icon = icon;
        }
    }

    /**
     * Gets the rank for a player based on their achievement count.
     */
    public static Rank getRank(Player player) {
        int achievementCount = AchievementManager.getUnlockedCount(player);
        return getRankForAchievements(achievementCount);
    }

    /**
     * Gets the rank for a specific achievement count.
     */
    public static Rank getRankForAchievements(int achievementCount) {
        Rank currentRank = Rank.INNOCENT;
        
        for (Rank rank : Rank.values()) {
            if (achievementCount >= rank.requiredAchievements) {
                currentRank = rank;
            } else {
                break;
            }
        }
        
        return currentRank;
    }

    /**
     * Gets the next rank after the current one, or null if max rank.
     */
    public static Rank getNextRank(Rank currentRank) {
        Rank[] ranks = Rank.values();
        int currentIndex = currentRank.ordinal();
        
        if (currentIndex + 1 < ranks.length) {
            return ranks[currentIndex + 1];
        }
        return null;
    }

    /**
     * Gets progress toward the next rank (0.0 to 1.0).
     */
    public static double getProgressToNextRank(Player player) {
        int achievements = AchievementManager.getUnlockedCount(player);
        Rank currentRank = getRankForAchievements(achievements);
        Rank nextRank = getNextRank(currentRank);
        
        if (nextRank == null) {
            return 1.0; // Max rank
        }
        
        int currentThreshold = currentRank.requiredAchievements;
        int nextThreshold = nextRank.requiredAchievements;
        int range = nextThreshold - currentThreshold;
        int progress = achievements - currentThreshold;
        
        return (double) progress / range;
    }

    /**
     * Gets achievements needed for next rank.
     */
    public static int getAchievementsToNextRank(Player player) {
        int achievements = AchievementManager.getUnlockedCount(player);
        Rank currentRank = getRankForAchievements(achievements);
        Rank nextRank = getNextRank(currentRank);
        
        if (nextRank == null) {
            return 0; // Max rank
        }
        
        return nextRank.requiredAchievements - achievements;
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
