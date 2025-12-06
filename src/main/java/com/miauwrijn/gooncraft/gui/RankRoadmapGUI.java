package com.miauwrijn.gooncraft.gui;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.miauwrijn.gooncraft.managers.AchievementManager;
import com.miauwrijn.gooncraft.managers.RankManager;
import com.miauwrijn.gooncraft.ranks.BaseRank;

/**
 * GUI showing rank progression in a snake-shaped connected pattern.
 * Displays ranks with visual path connections and pagination.
 */
public class RankRoadmapGUI extends GUI {

    private final Player target;
    private int page = 0;
    private static final int RANKS_PER_PAGE = 28; // 4 rows of 7 ranks (with snake pattern)
    
    // Snake pattern positions - winds through rows 1-4, columns 1-7
    // Pattern: row 1 → right, row 2 ← left, row 3 → right, row 4 ← left
    private static int[] generateSnakePattern() {
        int[] pattern = new int[28];
        int index = 0;
        
        // Row 1: left to right (col 1-7)
        for (int col = 1; col <= 7; col++) {
            pattern[index++] = slot(1, col);
        }
        
        // Row 2: right to left (col 7-1)
        for (int col = 7; col >= 1; col--) {
            pattern[index++] = slot(2, col);
        }
        
        // Row 3: left to right (col 1-7)
        for (int col = 1; col <= 7; col++) {
            pattern[index++] = slot(3, col);
        }
        
        // Row 4: right to left (col 7-1)
        for (int col = 7; col >= 1; col--) {
            pattern[index++] = slot(4, col);
        }
        
        return pattern;
    }
    
    private static final int[] SNAKE_PATTERN = generateSnakePattern();

    public RankRoadmapGUI(Player viewer, Player target) {
        super(viewer, "§6§l✦ Rank Roadmap ✦", 6);
        this.target = target;
        render();
    }

    private void render() {
        inventory.clear();
        clickHandlers.clear();
        
        BaseRank currentRank = RankManager.getRank(target);
        int unlockedAchievements = AchievementManager.getUnlockedCount(target);
        BaseRank[] allRanks = RankManager.getAllRanks();
        int currentRankIndex = currentRank.getOrdinal();
        
        // Fill with dark background
        fill(ItemBuilder.filler(Material.BLACK_STAINED_GLASS_PANE));
        
        // Top border
        fillBorder(ItemBuilder.filler(Material.ORANGE_STAINED_GLASS_PANE));
        
        // Calculate pagination
        int totalPages = (int) Math.ceil(allRanks.length / (double) RANKS_PER_PAGE);
        int startIndex = page * RANKS_PER_PAGE;
        int endIndex = Math.min(startIndex + RANKS_PER_PAGE, allRanks.length);
        int ranksOnThisPage = endIndex - startIndex;
        
        // Draw path background first (shows the snake path)
        drawSnakePathBackground(currentRankIndex, startIndex, ranksOnThisPage);
        
        // Draw ranks in snake pattern
        for (int i = 0; i < ranksOnThisPage && i < SNAKE_PATTERN.length; i++) {
            int rankIndex = startIndex + i;
            BaseRank rank = allRanks[rankIndex];
            int slotIndex = SNAKE_PATTERN[i];
            
            boolean isUnlocked = rankIndex <= currentRankIndex;
            boolean isCurrent = rankIndex == currentRankIndex;
            boolean isNext = rankIndex == currentRankIndex + 1;
            
            setItem(slotIndex, createRankItem(rank, isUnlocked, isCurrent, isNext, unlockedAchievements).build());
        }
        
        // Bottom navigation row (row 5)
        drawNavigation(currentRank, currentRankIndex, unlockedAchievements, allRanks.length, totalPages);
    }
    
    private void drawSnakePathBackground(int currentRankIndex, int startIndex, int ranksOnPage) {
        // Fill the snake path area with a subtle background color to show the connected path
        // Use lighter background for unlocked path, darker for locked
        for (int i = 0; i < ranksOnPage && i < SNAKE_PATTERN.length; i++) {
            int rankIndex = startIndex + i;
            int slotIndex = SNAKE_PATTERN[i];
            
            // Only set background if slot is empty (ranks will overlay)
            if (inventory.getItem(slotIndex) == null) {
                boolean pathUnlocked = rankIndex <= currentRankIndex;
                Material bgMaterial = pathUnlocked 
                    ? Material.LIME_STAINED_GLASS_PANE 
                    : Material.GRAY_STAINED_GLASS_PANE;
                
                setItem(slotIndex, new ItemBuilder(bgMaterial)
                        .name(" ")
                        .build());
            }
        }
    }
    
    private void drawNavigation(BaseRank currentRank, int currentRankIndex, int unlockedAchievements, 
                                int totalRanks, int totalPages) {
        // Back button
        setItem(slot(5, 0), new ItemBuilder(Material.ARROW)
                .name("§c§l← Back")
                .lore("§7Return to stats")
                .build(),
                event -> new StatsGUI(viewer, target).open());
        
        // Previous page
        if (page > 0) {
            setItem(slot(5, 3), new ItemBuilder(Material.ARROW)
                    .name("§e§l← Previous")
                    .lore("§7Page " + page + "/" + (totalPages - 1))
                    .build(),
                    event -> {
                        page--;
                        render();
                    });
        }
        
        // Page indicator and info
        setItem(slot(5, 4), new ItemBuilder(Material.BOOK)
                .name("§6§lPage " + (page + 1) + "/" + Math.max(1, totalPages))
                .lore(
                    "",
                    "§7Current Rank: " + currentRank.getDisplayName(),
                    "§7Rank #§e" + (currentRankIndex + 1) + "§7/§e" + totalRanks,
                    "§7Achievements: §e" + unlockedAchievements + "§7/§e67",
                    "",
                    "§8Unlock more achievements",
                    "§8to rank up!"
                )
                .build());
        
        // Next page button (only show if there are more ranks)
        if ((page + 1) * RANKS_PER_PAGE < totalRanks) {
            setItem(slot(5, 5), new ItemBuilder(Material.ARROW)
                    .name("§e§lNext →")
                    .lore("§7Page " + (page + 2) + "/" + totalPages)
                    .build(),
                    event -> {
                        page++;
                        render();
                    });
        }
        
        // Close button
        setItem(slot(5, 8), new ItemBuilder(Material.BARRIER)
                .name("§c§lClose")
                .lore("§7Click to close")
                .build(),
                event -> viewer.closeInventory());
    }

    private ItemBuilder createRankItem(BaseRank rank, boolean isUnlocked, boolean isCurrent, boolean isNext, int unlockedAchievements) {
        String statusLine;
        if (isCurrent) {
            statusLine = "§a§l✓ CURRENT RANK";
        } else if (isUnlocked) {
            statusLine = "§a✓ Unlocked";
        } else if (isNext) {
            int needed = rank.getRequiredAchievements() - unlockedAchievements;
            statusLine = "§e⚡ " + needed + " more achievements needed";
        } else {
            statusLine = "§8✗ Locked (" + rank.getRequiredAchievements() + " achievements)";
        }
        
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
        
        // Build lore with description, perks, and rewards
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add("§7Rank: §f#" + (rank.getOrdinal() + 1));
        lore.add("§7Requires: §f" + rank.getRequiredAchievements() + " achievements");
        
        // Add description if available
        String description = rank.getDescription();
        if (description != null && !description.isEmpty()) {
            lore.add("");
            lore.add("§8" + description);
        }
        
        // Add perks if available
        List<String> perks = rank.getPerkDescriptions();
        if (perks != null && !perks.isEmpty()) {
            lore.add("");
            lore.add("§a§lPerks:");
            for (String perk : perks) {
                lore.add("§a  • " + perk);
            }
        }
        
        
        lore.add("");
        lore.add(statusLine);
        
        ItemBuilder builder = new ItemBuilder(material)
                .name(rank.getIcon() + " " + rank.getDisplayName())
                .lore(lore);
        
        if (isCurrent || isUnlocked) {
            builder.glow();
        }
        
        return builder;
    }
}