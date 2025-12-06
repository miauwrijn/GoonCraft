package com.miauwrijn.gooncraft.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.miauwrijn.gooncraft.Plugin;
import com.miauwrijn.gooncraft.achievements.BaseAchievement;
import com.miauwrijn.gooncraft.achievements.StatAchievement;
import com.miauwrijn.gooncraft.data.PlayerStats;
import com.miauwrijn.gooncraft.managers.AchievementManager;
import com.miauwrijn.gooncraft.managers.StatisticsManager;

/**
 * GUI for displaying player achievements.
 */
public class AchievementsGUI extends GUI {

    private final Player target;
    private int page = 0;
    private String categoryFilter = null;
    private String sortType = "category"; // "alphabetical", "rarity", "category"

    public AchievementsGUI(Player viewer, Player target) {
        super(viewer, "§6§l" + target.getName() + "'s Achievements", 6);
        this.target = target;
        render();
    }

    private void render() {
        inventory.clear();
        clickHandlers.clear();
        
        Set<String> unlocked = AchievementManager.getUnlocked(target);
        PlayerStats stats = StatisticsManager.getStats(target);
        
        // Top border with category filters
        fillBorder(ItemBuilder.filler(Material.PURPLE_STAINED_GLASS_PANE));
        
        // Category filter buttons (only the 5 valid categories)
        setCategoryButton(slot(0, 2), "location", "§b§lLocation", Material.COMPASS, unlocked);
        setCategoryButton(slot(0, 3), "gooning", "§d§lGooning", Material.BONE, unlocked);
        setCategoryButton(slot(0, 4), "exposure", "§e§lExposure", Material.CLOCK, unlocked);
        setCategoryButton(slot(0, 5), "advanced", "§6§lAdvanced", Material.NETHER_STAR, unlocked);
        setCategoryButton(slot(0, 6), "hidden", "§5§l???", Material.ENDER_EYE, unlocked);
        
        // Get filtered achievements from YAML
        List<BaseAchievement> achievements = AchievementManager.getAchievementsByCategory(categoryFilter);
        
        // Sort achievements
        achievements = sortAchievements(achievements, sortType);
        
        // Display achievements (28 per page - 4 rows of 7)
        int startIndex = page * 28;
        int slot = 10;
        
        for (int i = startIndex; i < Math.min(startIndex + 28, achievements.size()); i++) {
            BaseAchievement achievement = achievements.get(i);
            String achievementId = achievement.getId().toLowerCase();
            boolean isUnlocked = unlocked.contains(achievementId);
            
            long currentValue = 0;
            long threshold = achievement.getThreshold();
            
            // Get progress for stat-based achievements
            if (achievement instanceof StatAchievement statAchievement) {
                currentValue = getStatForCategory(stats, statAchievement.getStatCategory());
            } else {
                // For non-stat achievements, use getCurrentProgress
                currentValue = achievement.getCurrentProgress(target, stats);
            }
            
            int progress = threshold > 0 ? (int) Math.min(100, (currentValue * 100) / threshold) : 0;
            
            setItem(slot, createAchievementItem(achievement, isUnlocked, progress, currentValue));
            
            slot++;
            // Skip border slots
            if ((slot + 1) % 9 == 0) {
                slot += 2;
            }
            if (slot >= 44) break;
        }
        
        // Fill empty achievement slots
        for (int row = 1; row <= 4; row++) {
            for (int col = 1; col <= 7; col++) {
                int s = slot(row, col);
                if (inventory.getItem(s) == null) {
                    setItem(s, ItemBuilder.filler(Material.BLACK_STAINED_GLASS_PANE));
                }
            }
        }
        
        // Bottom navigation
        int totalPages = (int) Math.ceil(achievements.size() / 28.0);
        
        // Stats button
        setItem(slot(5, 1), new ItemBuilder(Material.BOOK)
                .name("§a§lBack to Stats")
                .lore("§7Click to view stats")
                .build(),
                event -> new StatsGUI(viewer, target).open());
        
        // Previous page
        if (page > 0) {
            setItem(slot(5, 3), new ItemBuilder(Material.ARROW)
                    .name("§e§lPrevious Page")
                    .lore("§7Page " + page + "/" + totalPages)
                    .build(),
                    event -> {
                        page--;
                        render();
                    });
        }
        
        // Page indicator
        setItem(slot(5, 4), new ItemBuilder(Material.PAPER)
                .name("§6§lPage " + (page + 1) + "/" + Math.max(1, totalPages))
                .lore(
                    "",
                    "§7Unlocked: §e" + unlocked.size() + "§7/§e" + AchievementManager.getTotalAchievements(),
                    "",
                    categoryFilter != null ? "§7Filter: §e" + getCategoryName(categoryFilter) : "§7Showing all",
                    "§7Sort: §e" + getSortTypeName(sortType),
                    "",
                    "§7Rarities: §fCommon §aUncommon §bRare §5Mythic §6Legendary"
                )
                .build());
        
        // Sort buttons (bottom row)
        setSortButton(slot(5, 6), "alphabetical", "§e§lA-Z", Material.BOOK, sortType);
        setSortButton(slot(5, 7), "rarity", "§6§lRarity", Material.EMERALD, sortType);
        setSortButton(slot(5, 8), "category", "§b§lCategory", Material.COMPASS, sortType);
        
        // Next page
        if ((page + 1) * 28 < achievements.size()) {
            setItem(slot(5, 5), new ItemBuilder(Material.ARROW)
                    .name("§e§lNext Page")
                    .lore("§7Page " + (page + 2) + "/" + totalPages)
                    .build(),
                    event -> {
                        page++;
                        render();
                    });
        }
        
        // Clear filter (move to slot 5, 2)
        if (categoryFilter != null) {
            setItem(slot(5, 2), new ItemBuilder(Material.HOPPER)
                    .name("§c§lClear Filter")
                    .lore("§7Show all achievements")
                    .build(),
                    event -> {
                        categoryFilter = null;
                        page = 0;
                        render();
                    });
        }
        
        // Close button
        setItem(slot(4, 8), new ItemBuilder(Material.BARRIER)
                .name("§c§lClose")
                .lore("§7Click to close")
                .build(),
                event -> viewer.closeInventory());
    }

    private void setCategoryButton(int slot, String category, String name, Material material, Set<String> unlocked) {
        List<BaseAchievement> categoryAchievements = AchievementManager.getAchievementsByCategory(category);
        int total = categoryAchievements.size();
        int unlockedCount = 0;
        
        for (BaseAchievement achievement : categoryAchievements) {
            String achievementId = achievement.getId().toLowerCase();
            if (unlocked.contains(achievementId)) {
                unlockedCount++;
            }
        }
        
        boolean isSelected = category.equals(categoryFilter);
        
        ItemBuilder builder = new ItemBuilder(material)
                .name(name)
                .lore(
                    "§7Unlocked: §e" + unlockedCount + "§7/§e" + total,
                    "",
                    isSelected ? "§aCurrently filtering" : "§eClick to filter"
                )
                .hideFlags();
        
        if (isSelected) {
            builder.glow();
        }
        
        setItem(slot, builder.build(), event -> {
            if (category.equals(categoryFilter)) {
                categoryFilter = null;
            } else {
                categoryFilter = category;
            }
            page = 0;
            render();
        });
    }

    private org.bukkit.inventory.ItemStack createAchievementItem(BaseAchievement achievement, boolean isUnlocked, int progress, long currentValue) {
        // Hidden achievements show as ??? until unlocked
        boolean isHidden = achievement.isHidden() && !isUnlocked;
        
        // Material based on rarity
        Material material = getRarityMaterial(achievement.getRarity(), isUnlocked, isHidden);
        
        String displayName = isHidden ? "§5§k???" : achievement.getName();
        String displayDesc = isHidden ? "§5§oDiscover this secret..." : achievement.getDescription();
        String status = isUnlocked ? "§a§lUNLOCKED" : (isHidden ? "§5§lHIDDEN" : "§c§lLOCKED");
        
        // Get rarity display
        String rarityDisplay = achievement.getRarityDisplayName();
        
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add("§7" + displayDesc);
        lore.add("");
        lore.add("§7Rarity: " + rarityDisplay);
        lore.add("");
        lore.add(status);
        
        if (!isUnlocked && !isHidden && achievement.getThreshold() > 0) {
            lore.add("");
            lore.add("§7Progress: §e" + currentValue + "§7/§e" + achievement.getThreshold());
            lore.add(createProgressBar(progress));
        } else if (isHidden) {
            lore.add("");
            lore.add("§5§oFind the easter egg to unlock!");
        }
        
        // Use rarity color for unlocked achievements, muted for locked
        String nameColor = isUnlocked ? achievement.getRarityColor() : (isHidden ? "§5" : "§7");
        ItemBuilder builder = new ItemBuilder(material)
                .name(nameColor + displayName)
                .lore(lore);
        
        if (isUnlocked) {
            builder.glow();
        }
        
        return builder.build();
    }
    
    /**
     * Get material based on rarity.
     */
    private Material getRarityMaterial(String rarity, boolean isUnlocked, boolean isHidden) {
        if (isHidden) {
            return Material.PURPLE_DYE;
        }
        if (!isUnlocked) {
            return Material.GRAY_DYE;
        }
        
        // Unlocked achievements use rarity-based materials
        return switch (rarity) {
            case "legendary" -> Material.NETHERITE_INGOT;
            case "mythic" -> Material.AMETHYST_SHARD;
            case "rare" -> Material.DIAMOND;
            case "uncommon" -> Material.EMERALD;
            case "common" -> Material.IRON_INGOT;
            default -> Material.LIME_DYE;
        };
    }

    private String createProgressBar(int percent) {
        StringBuilder bar = new StringBuilder("§8[");
        int filled = percent / 5; // 20 chars total
        
        for (int i = 0; i < 20; i++) {
            if (i < filled) {
                bar.append("§a|");
            } else {
                bar.append("§7|");
            }
        }
        
        bar.append("§8] §e").append(percent).append("%");
        return bar.toString();
    }

    private long getStatForCategory(PlayerStats stats, String category) {
        return switch (category) {
            case "goon" -> stats.goonCount;
            case "cum_on" -> stats.cumOnOthersCount;
            case "got_cummed" -> stats.gotCummedOnCount;
            case "time_out" -> stats.getCurrentTotalTime();
            case "bf_given" -> stats.buttfingersGiven;
            case "bf_received" -> stats.buttfingersReceived;
            case "viagra" -> stats.viagraUsed;
            default -> 0;
        };
    }

    private String getCategoryName(String category) {
        if (category == null) return "All";
        
        return switch (category.toLowerCase()) {
            case "location" -> "Location";
            case "gooning" -> "Gooning";
            case "exposure" -> "Exposure";
            case "advanced" -> "Advanced";
            case "hidden" -> "???";
            default -> {
                // Log warning for invalid category
                Plugin.instance.getLogger().warning(
                    "AchievementsGUI: Unknown category '" + category + "' requested. Using 'Unknown'."
                );
                yield "Unknown";
            }
        };
    }
    
    private String getSortTypeName(String sortType) {
        return switch (sortType) {
            case "alphabetical" -> "A-Z";
            case "rarity" -> "Rarity";
            case "category" -> "Category";
            default -> "Unknown";
        };
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
    
    private List<BaseAchievement> sortAchievements(List<BaseAchievement> achievements, String sortType) {
        List<BaseAchievement> sorted = new ArrayList<>(achievements);
        
        switch (sortType) {
            case "alphabetical" -> sorted.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
            case "rarity" -> sorted.sort((a, b) -> {
                int rarityCompare = Integer.compare(b.getRarityOrder(), a.getRarityOrder()); // Higher rarity first
                if (rarityCompare != 0) return rarityCompare;
                return a.getName().compareToIgnoreCase(b.getName()); // Then alphabetical
            });
            case "category" -> sorted.sort((a, b) -> {
                int categoryCompare = a.getCategory().compareToIgnoreCase(b.getCategory());
                if (categoryCompare != 0) return categoryCompare;
                int rarityCompare = Integer.compare(b.getRarityOrder(), a.getRarityOrder());
                if (rarityCompare != 0) return rarityCompare;
                return a.getName().compareToIgnoreCase(b.getName());
            });
        }
        
        return sorted;
    }
}
