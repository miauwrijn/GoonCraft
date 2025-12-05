package com.miauwrijn.gooncraft.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.miauwrijn.gooncraft.managers.AchievementManager;
import com.miauwrijn.gooncraft.managers.RankManager;
import com.miauwrijn.gooncraft.managers.RankManager.Rank;

/**
 * GUI showing rank progression in an S-shaped roadmap pattern.
 * Displays all 12 ranks with visual progression path.
 */
public class RankRoadmapGUI extends GUI {

    // S-shaped pattern positions for 12 ranks in a 6-row inventory
    // Row 0: rank goes right →
    // Row 1: continues right, then down ↓
    // Row 2: goes left ←
    // Row 3: continues left, then down ↓
    // Row 4: goes right →
    // Row 5: final rank
    private static final int[][] RANK_POSITIONS = {
        // {row, col} for each rank (0-11)
        {0, 1},  // Rank 0: Innocent Virgin
        {0, 3},  // Rank 1: Curious Toucher
        {0, 5},  // Rank 2: Amateur Stroker
        {1, 7},  // Rank 3: Goon Enthusiast (turn down)
        {2, 5},  // Rank 4: Dedicated Degenerate (go left)
        {2, 3},  // Rank 5: Advanced Coomer
        {2, 1},  // Rank 6: Professional Gooner
        {3, 1},  // Rank 7: Master Bater (turn down)
        {4, 3},  // Rank 8: Elite Exhibitionist (go right)
        {4, 5},  // Rank 9: Legendary Pervert
        {4, 7},  // Rank 10: Golden Gooner
        {5, 4},  // Rank 11: ULTIMATE DEGENERATE (center bottom)
    };

    // Path connectors between ranks
    private static final int[][] PATH_POSITIONS = {
        // {row, col} for path pieces
        {0, 2},  // Between rank 0-1
        {0, 4},  // Between rank 1-2
        {0, 6}, {0, 7}, {1, 7},  // Curve to rank 3
        {1, 6}, {2, 7}, {2, 6},  // Down and left to rank 4
        {2, 4},  // Between rank 4-5
        {2, 2},  // Between rank 5-6
        {3, 0}, {3, 1}, {4, 1}, {4, 2},  // Curve down to rank 8
        {4, 4},  // Between rank 8-9
        {4, 6},  // Between rank 9-10
        {5, 7}, {5, 6}, {5, 5},  // Curve to final rank
    };

    private final Player target;

    public RankRoadmapGUI(Player viewer, Player target) {
        super(viewer, "§6§l✦ Rank Roadmap ✦", 6);
        this.target = target;
        
        Rank currentRank = RankManager.getRank(target);
        int unlockedAchievements = AchievementManager.getUnlockedCount(target);
        
        // Fill with dark background
        fill(ItemBuilder.filler(Material.BLACK_STAINED_GLASS_PANE));
        
        // Draw path connectors first (so ranks overlay them)
        drawPath(currentRank);
        
        // Draw all ranks
        drawRanks(currentRank, unlockedAchievements);
        
        // Back button
        setItem(slot(5, 0), new ItemBuilder(Material.ARROW)
                .name("§c§l← Back")
                .lore("§7Return to stats")
                .build(),
                event -> new StatsGUI(viewer, target).open());
        
        // Info in bottom right
        setItem(slot(5, 8), new ItemBuilder(Material.BOOK)
                .name("§e§lYour Progress")
                .lore(
                    "",
                    "§7Current Rank: " + currentRank.displayName,
                    "§7Achievements: §e" + unlockedAchievements + "§7/§e67",
                    "",
                    "§8Unlock more achievements",
                    "§8to rank up!"
                )
                .build());
    }

    private void drawPath(Rank currentRank) {
        int currentRankIndex = currentRank.ordinal();
        
        // Draw simple path markers between ranks
        for (int i = 0; i < RANK_POSITIONS.length - 1; i++) {
            int[] from = RANK_POSITIONS[i];
            int[] to = RANK_POSITIONS[i + 1];
            
            // Determine if this path segment is unlocked
            boolean unlocked = i < currentRankIndex;
            Material pathMaterial = unlocked ? Material.LIME_STAINED_GLASS_PANE : Material.GRAY_STAINED_GLASS_PANE;
            
            // Draw path between ranks
            drawPathBetween(from[0], from[1], to[0], to[1], pathMaterial, unlocked);
        }
    }

    private void drawPathBetween(int fromRow, int fromCol, int toRow, int toCol, Material material, boolean unlocked) {
        // Simple linear interpolation for path drawing
        int rowDiff = toRow - fromRow;
        int colDiff = toCol - fromCol;
        int steps = Math.max(Math.abs(rowDiff), Math.abs(colDiff));
        
        if (steps <= 1) return;
        
        for (int step = 1; step < steps; step++) {
            int row = fromRow + (rowDiff * step) / steps;
            int col = fromCol + (colDiff * step) / steps;
            
            int slotIndex = slot(row, col);
            if (slotIndex >= 0 && slotIndex < 54) {
                String pathChar = unlocked ? "§a═" : "§8═";
                setItem(slotIndex, new ItemBuilder(material)
                        .name(pathChar)
                        .build());
            }
        }
    }

    private void drawRanks(Rank currentRank, int unlockedAchievements) {
        Rank[] ranks = Rank.values();
        int currentRankIndex = currentRank.ordinal();
        
        for (int i = 0; i < ranks.length && i < RANK_POSITIONS.length; i++) {
            Rank rank = ranks[i];
            int[] pos = RANK_POSITIONS[i];
            int slotIndex = slot(pos[0], pos[1]);
            
            boolean isUnlocked = i <= currentRankIndex;
            boolean isCurrent = i == currentRankIndex;
            boolean isNext = i == currentRankIndex + 1;
            
            Material material;
            if (isCurrent) {
                material = Material.NETHER_STAR;
            } else if (isUnlocked) {
                material = Material.EMERALD;
            } else if (isNext) {
                material = Material.GOLD_INGOT;
            } else {
                material = Material.COAL;
            }
            
            // Calculate progress to this rank
            int achievementsNeeded = rank.requiredAchievements;
            int achievementsHave = unlockedAchievements;
            
            String statusLine;
            if (isCurrent) {
                statusLine = "§a§l✓ CURRENT RANK";
            } else if (isUnlocked) {
                statusLine = "§a✓ Unlocked";
            } else if (isNext) {
                int needed = achievementsNeeded - achievementsHave;
                statusLine = "§e⚡ " + needed + " more achievements needed";
            } else {
                statusLine = "§8✗ Locked (" + achievementsNeeded + " achievements)";
            }
            
            // Build the item
            ItemBuilder builder = new ItemBuilder(material)
                    .name(rank.icon + " " + rank.displayName)
                    .lore(
                        "",
                        "§7Requires: §f" + achievementsNeeded + " achievements",
                        "",
                        statusLine
                    );
            
            if (isCurrent || isUnlocked) {
                builder.glow();
            }
            
            setItem(slotIndex, builder.build());
        }
    }
}
