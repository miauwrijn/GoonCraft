package com.miauwrijn.gooncraft.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.miauwrijn.gooncraft.data.PlayerStats;
import com.miauwrijn.gooncraft.managers.AchievementManager;
import com.miauwrijn.gooncraft.managers.RankManager;
import com.miauwrijn.gooncraft.managers.StatisticsManager;

/**
 * GUI for displaying player statistics.
 */
public class StatsGUI extends GUI {

    public StatsGUI(Player viewer, Player target) {
        super(viewer, "§6§l" + target.getName() + "'s Goon Stats", 5);
        
        PlayerStats stats = StatisticsManager.getStats(target);
        boolean isSelf = viewer.equals(target);
        
        // Fill border
        fillBorder(ItemBuilder.filler(Material.PURPLE_STAINED_GLASS_PANE));
        
        // Get rank info
        RankManager.Rank rank = RankManager.getRank(target);
        RankManager.Rank nextRank = RankManager.getNextRank(rank);
        int achievementsToNext = RankManager.getAchievementsToNextRank(target);
        double progress = RankManager.getProgressToNextRank(target);
        String progressBar = RankManager.createProgressBar(progress, 10);
        
        // Build rank progress lore
        String rankProgressLine = nextRank != null 
            ? "§7Next: " + nextRank.displayName + " §7(" + achievementsToNext + " more)"
            : "§d§lMAX RANK ACHIEVED!";
        
        // Player head in center top - click to open rank roadmap
        setItem(slot(1, 4), new ItemBuilder(Material.PLAYER_HEAD)
                .skullOwner(target)
                .name("§6§l" + target.getName())
                .lore(
                    "",
                    rank.icon + " " + rank.displayName,
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
                .name("§d§lFap Count")
                .lore(
                    "§7Total goons: §e" + stats.goonCount,
                    "",
                    "§8Crouch + swing to fap"
                )
                .glow()
                .build());
        
        // Cum on others
        setItem(slot(2, 2), new ItemBuilder(Material.GHAST_TEAR)
                .name("§f§lCummed on Others")
                .lore(
                    "§7Times cummed on others: §e" + stats.cumOnOthersCount,
                    "",
                    "§8Get close to players when fapping"
                )
                .build());
        
        // Got cummed on
        setItem(slot(2, 3), new ItemBuilder(Material.SLIME_BALL)
                .name("§a§lGot Cummed On")
                .lore(
                    "§7Times got cummed on: §e" + stats.gotCummedOnCount,
                    "",
                    "§8Stay close to fapping players"
                )
                .build());
        
        // Time with penis out
        setItem(slot(2, 5), new ItemBuilder(Material.CLOCK)
                .name("§e§lExposure Time")
                .lore(
                    "§7Time with penis out: §e" + stats.formatTime(stats.getCurrentTotalTime()),
                    "",
                    "§8Use /penis toggle"
                )
                .glow()
                .build());
        
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
        setItem(slot(3, 6), new ItemBuilder(Material.GOLD_INGOT)
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
        setItem(slot(3, 2), new ItemBuilder(Material.DIAMOND)
                .name("§b§lLeaderboard")
                .lore(
                    "§7Click to view the leaderboard!",
                    "",
                    "§7Compare your stats with others"
                )
                .glow()
                .build(),
                event -> new LeaderboardGUI(viewer).open());
        
        // Close button
        setItem(slot(3, 4), new ItemBuilder(Material.BARRIER)
                .name("§c§lClose")
                .lore("§7Click to close")
                .build(),
                event -> viewer.closeInventory());
        
        // Fill remaining with glass
        fill(ItemBuilder.filler());
    }
}
