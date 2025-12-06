package com.miauwrijn.gooncraft.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.miauwrijn.gooncraft.Plugin;
import com.miauwrijn.gooncraft.data.PlayerStats;
import com.miauwrijn.gooncraft.managers.AchievementManager;
import com.miauwrijn.gooncraft.managers.RankManager;
import com.miauwrijn.gooncraft.managers.StatisticsManager;

/**
 * GUI for displaying player statistics.
 */
public class StatsGUI extends GUI {

    private static final int EXPOSURE_TIME_SLOT = slot(2, 5);
    private final Player target;

    public StatsGUI(Player viewer, Player target) {
        super(viewer, "§6§l" + target.getName() + "'s Goon Stats", 5);
        
        this.target = target;
        PlayerStats stats = StatisticsManager.getStats(target);
        boolean isSelf = viewer.equals(target);
        
        // Fill border
        fillBorder(ItemBuilder.filler(Material.PURPLE_STAINED_GLASS_PANE));
        
        // Get rank info
        com.miauwrijn.gooncraft.ranks.BaseRank rank = RankManager.getRank(target);
        com.miauwrijn.gooncraft.ranks.BaseRank nextRank = RankManager.getNextRank(rank);
        int achievementsToNext = RankManager.getAchievementsToNextRank(target);
        double progress = RankManager.getProgressToNextRank(target);
        String progressBar = RankManager.createProgressBar(progress, 10);
        
        // Build rank progress lore
        String rankProgressLine = nextRank != null 
            ? "§7Next: " + nextRank.getDisplayName() + " §7(" + achievementsToNext + " more)"
            : "§d§lMAX RANK ACHIEVED!";
        
        // Player head in center top - click to open rank roadmap
        setItem(slot(1, 4), new ItemBuilder(Material.PLAYER_HEAD)
                .skullOwner(target)
                .name("§6§l" + target.getName())
                .lore(
                    "",
                    rank.getIcon() + " " + rank.getDisplayName(),
                    progressBar + " §7" + Math.round(progress * 100) + "%",
                    rankProgressLine,
                    "",
                    "§eAchievements: §f" + AchievementManager.getUnlockedCount(target) + 
                        "§7/§f" + AchievementManager.getTotalAchievements(),
                    "",
                    "§e§lClick to view Rank Roadmap!"
                )
                .build(),
                event -> new RankRoadmapGUI(viewer, target).open());
        
        // Fap count
        setItem(slot(2, 1), new ItemBuilder(Material.BONE)
                .name("§d§lGoon Count")
                .lore(
                    "§7Total goons: §e" + formatNumber(stats.goonCount),
                    "",
                    "§8Crouch + swing to fap"
                )
                .glow()
                .build());
        
        // Ejaculations / Orgasms
        setItem(slot(2, 2), new ItemBuilder(Material.GHAST_TEAR)
                .name("§f§lTotal Ejaculations")
                .lore(
                    "§7Total finishes: §e" + formatNumber(stats.totalEjaculations),
                    "§7On others: §e" + formatNumber(stats.cumOnOthersCount),
                    "",
                    "§8Keep gooning to finish!"
                )
                .build());
        
        // Goon streak
        ItemBuilder streakBuilder = new ItemBuilder(Material.BLAZE_POWDER)
                .name("§6§lGoon Streak")
                .lore(
                    "§7Current streak: §e" + stats.getStreakDisplay(),
                    "§7Best streak: §e" + stats.longestGoonStreak + " days",
                    "",
                    "§8Goon every MC day to keep your streak!"
                );
        if (stats.currentGoonStreak >= 7) {
            streakBuilder.glow(); // Glow for 7+ day streaks
        }
        setItem(slot(2, 3), streakBuilder.build());
        
        // Got cummed on (slot 2,4)
        setItem(slot(2, 4), new ItemBuilder(Material.SLIME_BALL)
                .name("§a§lGot Cummed On")
                .lore(
                    "§7Times got cummed on: §e" + formatNumber(stats.gotCummedOnCount),
                    "",
                    "§8Stay close to fapping players"
                )
                .build());
        
        // Time with genitals out (this slot will be updated dynamically)
        updateExposureTimeItem();
        
        // Buttfingers given
        setItem(slot(2, 6), new ItemBuilder(Material.CARROT)
                .name("§6§lButtfingers Given")
                .lore(
                    "§7Buttfingers given: §e" + stats.buttfingersGiven,
                    "",
                    "§8Use /buttfinger <player>"
                )
                .build());
        
        // Buttfingers received
        setItem(slot(2, 7), new ItemBuilder(Material.GOLDEN_CARROT)
                .name("§c§lButtfingers Received")
                .lore(
                    "§7Buttfingers received: §e" + stats.buttfingersReceived,
                    "",
                    "§8Ouch!"
                )
                .build());
        
        // Viagra used
        setItem(slot(3, 4), new ItemBuilder(Material.POTION)
                .name("§b§lViagra Used")
                .lore(
                    "§7Viagra consumed: §e" + stats.viagraUsed,
                    "",
                    "§8Craft or use /viagra"
                )
                .hideFlags()
                .build());
        
        // Achievements button
        setItem(slot(3, 2), new ItemBuilder(Material.GOLD_INGOT)
                .name("§6§lView Achievements")
                .lore(
                    "§7Click to view achievements!",
                    "",
                    "§e" + AchievementManager.getUnlockedCount(target) + "§7/§e" + 
                        AchievementManager.getTotalAchievements() + " §7unlocked"
                )
                .glow()
                .build(),
                event -> new AchievementsGUI(viewer, target).open());
        
        // Leaderboard button
        setItem(slot(3, 6), new ItemBuilder(Material.DIAMOND)
                .name("§b§lLeaderboard")
                .lore(
                    "§7Click to view the leaderboard!",
                    "",
                    "§7Compare your stats with others"
                )
                .glow()
                .build(),
                event -> new LeaderboardGUI(viewer).open());
        
        // Perk Management button (only show for self)
        if (isSelf) {
            setItem(slot(4, 6), new ItemBuilder(Material.ANVIL)
                    .name("§6§lPerk Management")
                    .lore(
                        "§7Manage your rank perks!",
                        "",
                        "§7Toggle perks on/off",
                        "",
                        "§e§lClick to open Perk Management!"
                    )
                    .glow()
                    .build(),
                    event -> new PerkManagementGUI(viewer, target).open());
        }
        
        // Close button
        setItem(slot(4, 4), new ItemBuilder(Material.BARRIER)
                .name("§c§lClose")
                .lore("§7Click to close")
                .build(),
                event -> viewer.closeInventory());
        
        // Fill remaining with glass
        fill(ItemBuilder.filler());
        
        // Start dynamic update task for exposure time
        startExposureTimeUpdater();
    }
    
    /**
     * Updates the exposure time item with current value.
     */
    private void updateExposureTimeItem() {
        PlayerStats stats = StatisticsManager.getStats(target);
        setItem(EXPOSURE_TIME_SLOT, new ItemBuilder(Material.CLOCK)
                .name("§e§lExposure Time")
                .lore(
                    "§7Time exposed: §e" + stats.formatTime(stats.getCurrentTotalTime()),
                    "",
                    "§8Use /penis toggle"
                )
                .glow()
                .build());
    }
    
    /**
     * Starts a task that updates the exposure time every second.
     */
    private void startExposureTimeUpdater() {
        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                // Check if player is still online and inventory is still open
                if (!viewer.isOnline() || !target.isOnline()) {
                    cancel();
                    return;
                }
                
                // Update the exposure time item
                updateExposureTimeItem();
            }
        }.runTaskTimer(Plugin.instance, 20L, 20L); // Update every second (20 ticks)
    }
    
    /**
     * Format large numbers with comma separators or K/M suffixes.
     */
    private String formatNumber(long number) {
        if (number >= 1_000_000) {
            return String.format("%.1fM", number / 1_000_000.0);
        } else if (number >= 10_000) {
            return String.format("%.1fK", number / 1_000.0);
        } else {
            return String.format("%,d", number);
        }
    }
}
