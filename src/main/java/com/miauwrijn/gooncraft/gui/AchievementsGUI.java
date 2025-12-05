package com.miauwrijn.gooncraft.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.miauwrijn.gooncraft.data.PlayerStats;
import com.miauwrijn.gooncraft.managers.AchievementManager;
import com.miauwrijn.gooncraft.managers.AchievementManager.Achievement;
import com.miauwrijn.gooncraft.managers.StatisticsManager;

/**
 * GUI for displaying player achievements.
 */
public class AchievementsGUI extends GUI {

    private final Player target;
    private int page = 0;
    private String categoryFilter = null;

    public AchievementsGUI(Player viewer, Player target) {
        super(viewer, "§6§l" + target.getName() + "'s Achievements", 6);
        this.target = target;
        render();
    }

    private void render() {
        inventory.clear();
        clickHandlers.clear();
        
        Set<Achievement> unlocked = AchievementManager.getUnlocked(target);
        PlayerStats stats = StatisticsManager.getStats(target);
        
        // Top border with category filters
        fillBorder(ItemBuilder.filler(Material.PURPLE_STAINED_GLASS_PANE));
        
        // Category filter buttons
        setCategoryButton(slot(0, 1), "fap", "§d§lFapping", Material.BONE, unlocked);
        setCategoryButton(slot(0, 2), "cum_on", "§f§lCumming", Material.GHAST_TEAR, unlocked);
        setCategoryButton(slot(0, 3), "got_cummed", "§a§lGot Cummed", Material.SLIME_BALL, unlocked);
        setCategoryButton(slot(0, 4), "time_out", "§e§lExposure", Material.CLOCK, unlocked);
        setCategoryButton(slot(0, 5), "bf_given", "§6§lButtfinger", Material.CARROT, unlocked);
        setCategoryButton(slot(0, 6), "bf_received", "§c§lGot BF'd", Material.GOLDEN_CARROT, unlocked);
        setCategoryButton(slot(0, 7), "hidden", "§5§l???", Material.ENDER_EYE, unlocked);
        
        // Get filtered achievements
        List<Achievement> achievements = new ArrayList<>();
        for (Achievement achievement : Achievement.values()) {
            if (categoryFilter == null || achievement.category.equals(categoryFilter)) {
                achievements.add(achievement);
            }
        }
        
        // Display achievements (28 per page - 4 rows of 7)
        int startIndex = page * 28;
        int slot = 10;
        
        for (int i = startIndex; i < Math.min(startIndex + 28, achievements.size()); i++) {
            Achievement achievement = achievements.get(i);
            boolean isUnlocked = unlocked.contains(achievement);
            
            long currentValue = getStatForCategory(stats, achievement.category);
            long threshold = achievement.threshold;
            int progress = (int) Math.min(100, (currentValue * 100) / threshold);
            
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
                    "§7Unlocked: §e" + unlocked.size() + "§7/§e" + Achievement.values().length,
                    "",
                    categoryFilter != null ? "§7Filter: §e" + getCategoryName(categoryFilter) : "§7Showing all"
                )
                .build());
        
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
        
        // Clear filter
        if (categoryFilter != null) {
            setItem(slot(5, 7), new ItemBuilder(Material.HOPPER)
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
        setItem(slot(5, 8), new ItemBuilder(Material.BARRIER)
                .name("§c§lClose")
                .lore("§7Click to close")
                .build(),
                event -> viewer.closeInventory());
    }

    private void setCategoryButton(int slot, String category, String name, Material material, Set<Achievement> unlocked) {
        int total = 0;
        int unlockedCount = 0;
        
        for (Achievement achievement : Achievement.values()) {
            if (achievement.category.equals(category)) {
                total++;
                if (unlocked.contains(achievement)) {
                    unlockedCount++;
                }
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

    private org.bukkit.inventory.ItemStack createAchievementItem(Achievement achievement, boolean isUnlocked, int progress, long currentValue) {
        // Hidden achievements show as ??? until unlocked
        boolean isHidden = achievement.hidden && !isUnlocked;
        
        Material material;
        if (isUnlocked) {
            material = Material.LIME_DYE;
        } else if (isHidden) {
            material = Material.PURPLE_DYE;
        } else {
            material = Material.GRAY_DYE;
        }
        
        String displayName = isHidden ? "§5§k???" : achievement.name;
        String displayDesc = isHidden ? "§5§oDiscover this secret..." : achievement.description;
        String status = isUnlocked ? "§a§lUNLOCKED" : (isHidden ? "§5§lHIDDEN" : "§c§lLOCKED");
        
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add("§7" + displayDesc);
        lore.add("");
        lore.add(status);
        
        if (!isUnlocked && !isHidden) {
            lore.add("");
            lore.add("§7Progress: §e" + currentValue + "§7/§e" + achievement.threshold);
            lore.add(createProgressBar(progress));
        } else if (isHidden) {
            lore.add("");
            lore.add("§5§oFind the easter egg to unlock!");
        }
        
        ItemBuilder builder = new ItemBuilder(material)
                .name((isUnlocked ? "§a" : (isHidden ? "§5" : "§7")) + displayName)
                .lore(lore);
        
        if (isUnlocked) {
            builder.glow();
        }
        
        return builder.build();
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
            case "fap" -> stats.fapCount;
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
        return switch (category) {
            case "fap" -> "Fapping";
            case "cum_on" -> "Cumming";
            case "got_cummed" -> "Got Cummed";
            case "time_out" -> "Exposure";
            case "bf_given" -> "Buttfinger";
            case "bf_received" -> "Got BF'd";
            case "viagra" -> "Viagra";
            case "hidden" -> "???";
            default -> "Unknown";
        };
    }
}
