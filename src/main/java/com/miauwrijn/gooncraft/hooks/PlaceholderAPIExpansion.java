package com.miauwrijn.gooncraft.hooks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.miauwrijn.gooncraft.Plugin;
import com.miauwrijn.gooncraft.data.PlayerStats;
import com.miauwrijn.gooncraft.managers.AchievementManager;
import com.miauwrijn.gooncraft.managers.GenderManager;
import com.miauwrijn.gooncraft.managers.RankManager;
import com.miauwrijn.gooncraft.managers.StatisticsManager;
import com.miauwrijn.gooncraft.ranks.BaseRank;
import com.miauwrijn.gooncraft.storage.StorageManager;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

/**
 * PlaceholderAPI expansion for GoonCraft.
 * 
 * Available placeholders:
 * 
 * === Personal Stats ===
 * %gooncraft_rank%           - Player's rank display name
 * %gooncraft_rank_name%      - Player's rank name (no colors)
 * %gooncraft_rank_ordinal%   - Player's rank number (0-indexed)
 * %gooncraft_rank_icon%      - Player's rank icon
 * %gooncraft_xp%             - Player's total XP
 * %gooncraft_xp_formatted%   - Player's XP with formatting (e.g., "1.5K")
 * %gooncraft_xp_next%        - XP needed for next rank
 * %gooncraft_xp_progress%    - Progress to next rank (0-100)
 * %gooncraft_xp_bar%         - Visual progress bar
 * 
 * %gooncraft_goons%          - Total goon count
 * %gooncraft_faps%           - Alias for goons
 * %gooncraft_cum_on%         - Times cummed on others
 * %gooncraft_got_cummed%     - Times got cummed on
 * %gooncraft_exposure_time%  - Formatted exposure time (e.g., "5h 30m")
 * %gooncraft_exposure_seconds% - Raw exposure time in seconds
 * %gooncraft_ejaculations%   - Total ejaculations/orgasms
 * %gooncraft_strokes%        - Total strokes (arm swings while gooning)
 * %gooncraft_streak%         - Current goon streak (MC days)
 * %gooncraft_streak_best%    - Best goon streak ever
 * %gooncraft_buttfingers_given%
 * %gooncraft_buttfingers_received%
 * %gooncraft_farts%
 * %gooncraft_poops%
 * %gooncraft_piss%
 * %gooncraft_viagra%
 * 
 * %gooncraft_achievements%   - Unlocked achievement count
 * %gooncraft_achievements_total% - Total achievements available
 * %gooncraft_achievements_percent% - Percentage unlocked
 * 
 * %gooncraft_gender%         - Player's gender
 * 
 * === Leaderboard Placeholders ===
 * %gooncraft_top_goons_<1-10>_name%   - Player name at position
 * %gooncraft_top_goons_<1-10>_value%  - Stat value at position
 * %gooncraft_top_xp_<1-10>_name%
 * %gooncraft_top_xp_<1-10>_value%
 * %gooncraft_top_time_<1-10>_name%
 * %gooncraft_top_time_<1-10>_value%
 * %gooncraft_top_achievements_<1-10>_name%
 * %gooncraft_top_achievements_<1-10>_value%
 * %gooncraft_top_cum_<1-10>_name%
 * %gooncraft_top_cum_<1-10>_value%
 * %gooncraft_top_buttfingers_<1-10>_name%
 * %gooncraft_top_buttfingers_<1-10>_value%
 */
public class PlaceholderAPIExpansion extends PlaceholderExpansion {

    private final Plugin plugin;

    public PlaceholderAPIExpansion(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "gooncraft";
    }

    @Override
    public String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors());
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true; // Stay registered on PAPI reload
    }

    @Override
    public String onRequest(OfflinePlayer offlinePlayer, String identifier) {
        if (offlinePlayer == null) {
            return "";
        }

        // Handle leaderboard placeholders first (don't need online player)
        if (identifier.startsWith("top_")) {
            return handleLeaderboardPlaceholder(identifier);
        }

        // For personal stats, we need an online player
        Player player = offlinePlayer.getPlayer();
        if (player == null) {
            return "";
        }

        PlayerStats stats = StatisticsManager.getStats(player);
        if (stats == null) {
            return "";
        }

        // Personal stats placeholders
        return switch (identifier.toLowerCase()) {
            // Rank placeholders
            case "rank" -> {
                BaseRank rank = RankManager.getRank(player);
                yield rank != null ? rank.getDisplayName() : "Unknown";
            }
            case "rank_name" -> {
                BaseRank rank = RankManager.getRank(player);
                yield rank != null ? stripColor(rank.getDisplayName()) : "Unknown";
            }
            case "rank_ordinal" -> {
                BaseRank rank = RankManager.getRank(player);
                yield rank != null ? String.valueOf(rank.getOrdinal()) : "0";
            }
            case "rank_icon" -> {
                BaseRank rank = RankManager.getRank(player);
                yield rank != null ? rank.getIcon() : "";
            }

            // XP placeholders
            case "xp" -> String.valueOf(stats.experience);
            case "xp_formatted" -> formatNumber(stats.experience);
            case "xp_next" -> {
                BaseRank nextRank = RankManager.getNextRank(RankManager.getRank(player));
                if (nextRank == null) yield "MAX";
                long xpNeeded = RankManager.getXpToNextRank(player);
                yield String.valueOf(xpNeeded);
            }
            case "xp_progress" -> {
                double progress = RankManager.getProgressToNextRank(player);
                yield String.valueOf(Math.round(progress * 100));
            }
            case "xp_bar" -> {
                double progress = RankManager.getProgressToNextRank(player);
                yield RankManager.createProgressBar(progress, 10);
            }

            // Core stats
            case "goons", "faps" -> String.valueOf(stats.goonCount);
            case "cum_on" -> String.valueOf(stats.cumOnOthersCount);
            case "got_cummed" -> String.valueOf(stats.gotCummedOnCount);
            case "exposure_time" -> stats.formatTime(stats.getCurrentTotalTime());
            case "exposure_seconds" -> String.valueOf(stats.getCurrentTotalTime());
            case "buttfingers_given" -> String.valueOf(stats.buttfingersGiven);
            case "buttfingers_received" -> String.valueOf(stats.buttfingersReceived);
            case "farts" -> String.valueOf(stats.fartCount);
            case "poops" -> String.valueOf(stats.poopCount);
            case "piss" -> String.valueOf(stats.pissCount);
            case "viagra" -> String.valueOf(stats.viagraUsed);
            
            // Detailed goon stats
            case "ejaculations", "orgasms" -> String.valueOf(stats.totalEjaculations);
            case "strokes" -> String.valueOf(stats.totalStrokes);
            case "streak" -> stats.getStreakDisplay();
            case "streak_days" -> String.valueOf(stats.currentGoonStreak);
            case "streak_best" -> String.valueOf(stats.longestGoonStreak);

            // Achievement stats
            case "achievements" -> String.valueOf(AchievementManager.getUnlockedCount(player));
            case "achievements_total" -> String.valueOf(AchievementManager.getTotalAchievements());
            case "achievements_percent" -> {
                int unlocked = AchievementManager.getUnlockedCount(player);
                int total = AchievementManager.getTotalAchievements();
                if (total == 0) yield "0";
                yield String.valueOf(Math.round((double) unlocked / total * 100));
            }

            // Gender
            case "gender" -> {
                GenderManager.Gender gender = GenderManager.getGender(player);
                yield gender != null ? gender.getDisplayName() : "Not Set";
            }

            // Boob/jiggle stats (for female/other)
            case "jiggles" -> String.valueOf(stats.jiggleCount);
            case "boob_toggles" -> String.valueOf(stats.boobToggleCount);

            default -> null;
        };
    }

    /**
     * Handle leaderboard placeholders like %gooncraft_top_goons_1_name%
     */
    private String handleLeaderboardPlaceholder(String identifier) {
        // Format: top_<category>_<position>_<name|value>
        String[] parts = identifier.split("_");
        if (parts.length < 4) return "";

        String category = parts[1];
        int position;
        try {
            position = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            return "";
        }

        if (position < 1 || position > 10) return "";

        String type = parts[3]; // "name" or "value"

        // Get sorted list of players for this category
        List<LeaderboardEntry> entries = getLeaderboardEntries(category);
        if (entries.isEmpty() || position > entries.size()) {
            return type.equals("name") ? "-" : "0";
        }

        LeaderboardEntry entry = entries.get(position - 1);
        if (type.equals("name")) {
            return entry.name;
        } else if (type.equals("value")) {
            if (category.equals("time")) {
                return formatTime(entry.value);
            }
            return formatNumber(entry.value);
        }

        return "";
    }

    /**
     * Get sorted leaderboard entries for a category.
     */
    private List<LeaderboardEntry> getLeaderboardEntries(String category) {
        List<LeaderboardEntry> entries = new ArrayList<>();

        // Get all online players and their stats
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerStats stats = StatisticsManager.getStats(player);
            if (stats == null) continue;

            long value = switch (category.toLowerCase()) {
                case "goons", "faps" -> stats.goonCount;
                case "xp" -> stats.experience;
                case "time" -> stats.getCurrentTotalTime();
                case "achievements" -> AchievementManager.getUnlockedCount(player);
                case "cum", "ejaculations" -> stats.totalEjaculations;
                case "strokes" -> stats.totalStrokes;
                case "streak" -> stats.currentGoonStreak;
                case "buttfingers", "bf" -> stats.buttfingersGiven;
                case "farts" -> stats.fartCount;
                case "poops" -> stats.poopCount;
                case "piss" -> stats.pissCount;
                default -> stats.goonCount;
            };

            entries.add(new LeaderboardEntry(player.getName(), value));
        }

        // Sort by value descending
        entries.sort(Comparator.comparingLong((LeaderboardEntry e) -> e.value).reversed());

        return entries;
    }

    /**
     * Format large numbers (e.g., 1500 -> "1.5K")
     */
    private String formatNumber(long number) {
        if (number < 1000) return String.valueOf(number);
        if (number < 1000000) {
            double val = number / 1000.0;
            return String.format("%.1fK", val).replace(".0K", "K");
        }
        if (number < 1000000000) {
            double val = number / 1000000.0;
            return String.format("%.1fM", val).replace(".0M", "M");
        }
        double val = number / 1000000000.0;
        return String.format("%.1fB", val).replace(".0B", "B");
    }

    /**
     * Format time in seconds to readable format.
     */
    private String formatTime(long seconds) {
        if (seconds < 60) {
            return seconds + "s";
        } else if (seconds < 3600) {
            return (seconds / 60) + "m " + (seconds % 60) + "s";
        } else {
            long hours = seconds / 3600;
            long minutes = (seconds % 3600) / 60;
            return hours + "h " + minutes + "m";
        }
    }

    /**
     * Strip color codes from a string.
     */
    private String stripColor(String input) {
        if (input == null) return "";
        return input.replaceAll("§[0-9a-fk-or]", "");
    }

    /**
     * Simple record for leaderboard entries.
     */
    private record LeaderboardEntry(String name, long value) {}
}
