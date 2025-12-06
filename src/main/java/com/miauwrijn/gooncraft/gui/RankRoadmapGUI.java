package com.miauwrijn.gooncraft.gui;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.miauwrijn.gooncraft.managers.RankManager;
import com.miauwrijn.gooncraft.managers.StatisticsManager;
import com.miauwrijn.gooncraft.ranks.BaseRank;

/**
 * GUI showing rank progression in a snake-shaped connected pattern.
 * Displays ranks with visual path connections and pagination.
 */
public class RankRoadmapGUI extends GUI {

    private final Player target;
    private int page = 0;
    private String sortType = "required"; // "required", "rarity", "alphabetical"
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
        long playerXp = StatisticsManager.getStats(target).experience;
        BaseRank[] allRanks = RankManager.getAllRanks();
        
        // Sort ranks based on sortType
        List<BaseRank> sortedRanks = sortRanks(new ArrayList<>(List.of(allRanks)), sortType);
        allRanks = sortedRanks.toArray(new BaseRank[0]);
        
        // Recalculate current rank index after sorting
        int currentRankIndex = -1;
        for (int i = 0; i < allRanks.length; i++) {
            if (allRanks[i].getOrdinal() == currentRank.getOrdinal()) {
                currentRankIndex = i;
                break;
            }
        }
        if (currentRankIndex == -1) currentRankIndex = 0;
        
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
            
            setItem(slotIndex, createRankItem(rank, isUnlocked, isCurrent, isNext, playerXp).build());
        }
        
        // Bottom navigation row (row 5)
        drawNavigation(currentRank, currentRankIndex, playerXp, allRanks.length, totalPages);
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
    
    private void drawNavigation(BaseRank currentRank, int currentRankIndex, long playerXp, 
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
                    "§7XP: §e" + formatNumber(playerXp),
                    "",
                    "§8Earn more XP",
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
        
        // Sort buttons
        setSortButton(slot(5, 6), "required", "§e§lRequired", Material.EMERALD, sortType);
        setSortButton(slot(5, 7), "rarity", "§6§lRarity", Material.DIAMOND, sortType);
        setSortButton(slot(5, 8), "alphabetical", "§b§lA-Z", Material.BOOK, sortType);
        
        // Close button moved to slot 4, 8
        setItem(slot(4, 8), new ItemBuilder(Material.BARRIER)
                .name("§c§lClose")
                .lore("§7Click to close")
                .build(),
                event -> viewer.closeInventory());
    }
    
    private void setSortButton(int slot, String sortType, String name, Material material, String currentSort) {
        boolean isSelected = sortType.equals(currentSort);
        
        ItemBuilder builder = new ItemBuilder(material)
                .name(name)
                .lore(
                    "",
                    isSelected ? "§aCurrently sorting" : "§7Click to sort by " + getSortTypeName(sortType)
                )
                .hideFlags();
        
        if (isSelected) {
            builder.glow();
        }
        
        setItem(slot, builder.build(), event -> {
            this.sortType = sortType;
            page = 0; // Reset to first page when changing sort
            render();
        });
    }
    
    private String getSortTypeName(String sortType) {
        return switch (sortType) {
            case "required" -> "Required XP";
            case "rarity" -> "Rarity";
            case "alphabetical" -> "Alphabetical";
            default -> "Unknown";
        };
    }
    
    private List<BaseRank> sortRanks(List<BaseRank> ranks, String sortType) {
        List<BaseRank> sorted = new ArrayList<>(ranks);
        
        switch (sortType) {
            case "alphabetical" -> sorted.sort((a, b) -> a.getDisplayName().compareToIgnoreCase(b.getDisplayName()));
            case "rarity" -> sorted.sort((a, b) -> {
                int rarityCompare = Integer.compare(b.getRarityOrder(), a.getRarityOrder()); // Higher rarity first
                if (rarityCompare != 0) return rarityCompare;
                return Long.compare(a.getRequiredXp(), b.getRequiredXp()); // Then by required XP
            });
            case "required" -> sorted.sort((a, b) -> Long.compare(a.getRequiredXp(), b.getRequiredXp()));
        }
        
        return sorted;
    }

    private ItemBuilder createRankItem(BaseRank rank, boolean isUnlocked, boolean isCurrent, boolean isNext, long playerXp) {
        String statusLine;
        if (isCurrent) {
            statusLine = "§a§l✓ CURRENT RANK";
        } else if (isUnlocked) {
            statusLine = "§a✓ Unlocked";
        } else if (isNext) {
            long needed = rank.getRequiredXp() - playerXp;
            statusLine = "§e⚡ " + formatNumber(needed) + " more XP needed";
        } else {
            statusLine = "§8✗ Locked (" + formatNumber(rank.getRequiredXp()) + " XP)";
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
        lore.add("§7Requires: §f" + formatNumber(rank.getRequiredXp()) + " XP");
        
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
}