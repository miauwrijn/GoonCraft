package com.miauwrijn.gooncraft.gui;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import com.miauwrijn.gooncraft.data.PlayerStats;
import com.miauwrijn.gooncraft.managers.AchievementManager;
import com.miauwrijn.gooncraft.managers.RankManager;
import com.miauwrijn.gooncraft.managers.StatisticsManager;

/**
 * GUI for displaying the leaderboard.
 */
public class LeaderboardGUI extends GUI {

    private String currentCategory = "faps";
    
    public LeaderboardGUI(Player viewer) {
        super(viewer, "§6§lLeaderboard", 6);
        buildGUI();
    }

    private void buildGUI() {
        // Clear and rebuild
        for (int i = 0; i < 54; i++) {
            setItem(i, null);
        }
        
        // Top border
        for (int i = 0; i < 9; i++) {
            setItem(i, ItemBuilder.filler(Material.ORANGE_STAINED_GLASS_PANE));
        }
        
        // Category buttons (top row)
        setItem(1, createCategoryButton("faps", "§e§lFaps", Material.SLIME_BALL, "§7Total fap count"));
        setItem(2, createCategoryButton("cumon", "§6§lCummed On Others", Material.GHAST_TEAR, "§7Times cummed on others"));
        setItem(3, createCategoryButton("cummed", "§d§lGot Cummed On", Material.HONEYCOMB, "§7Times got cummed on"));
        setItem(4, createCategoryButton("time", "§b§lExposure Time", Material.CLOCK, "§7Time with genitals out"));
        setItem(5, createCategoryButton("bf", "§c§lButtfingers", Material.STICK, "§7Buttfingers given"));
        setItem(6, createCategoryButton("rank", "§5§lRank", Material.NETHER_STAR, "§7By achievement rank"));
        setItem(7, createCategoryButton("achievements", "§a§lAchievements", Material.DIAMOND, "§7By achievement count"));
        
        // Separator
        for (int i = 9; i < 18; i++) {
            setItem(i, ItemBuilder.filler(Material.BLACK_STAINED_GLASS_PANE));
        }
        
        // Display title
        setItem(13, new ItemBuilder(Material.OAK_SIGN)
                .name("§6§l" + getCategoryDisplayName(currentCategory))
                .lore("§7Currently viewing", "§eClick a category above", "§7to change the view")
                .build());
        
        // Leaderboard entries (slots 18-53, excluding bottom row)
        displayLeaderboard();
        
        // Bottom row - navigation and info
        for (int i = 45; i < 54; i++) {
            setItem(i, ItemBuilder.filler(Material.ORANGE_STAINED_GLASS_PANE));
        }
        
        // Close button
        setItem(49, new ItemBuilder(Material.BARRIER)
                .name("§c§lClose")
                .lore("§7Click to close")
                .build(),
                event -> viewer.closeInventory());
    }

    private void displayLeaderboard() {
        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        
        // Sort by current category
        onlinePlayers.sort((a, b) -> {
            long valA = getStatValue(a, currentCategory);
            long valB = getStatValue(b, currentCategory);
            return Long.compare(valB, valA);
        });
        
        // Display up to 27 players (3 rows of 9)
        int slot = 18;
        int rank = 1;
        
        for (Player p : onlinePlayers) {
            if (rank > 27 || slot >= 45) break;
            
            setItem(slot, createPlayerHead(p, rank));
            slot++;
            rank++;
        }
        
        // Fill remaining slots
        while (slot < 45) {
            setItem(slot, ItemBuilder.filler());
            slot++;
        }
    }

    private ItemStack createPlayerHead(Player player, int rank) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        
        if (meta != null) {
            meta.setOwningPlayer(player);
            
            // Medal for top 3
            String medal = switch (rank) {
                case 1 -> "§6🥇 ";
                case 2 -> "§7🥈 ";
                case 3 -> "§c🥉 ";
                default -> "§7#" + rank + " ";
            };
            
            String rankColor = switch (rank) {
                case 1 -> "§6§l";
                case 2 -> "§7§l";
                case 3 -> "§c§l";
                default -> "§f";
            };
            
            meta.setDisplayName(medal + rankColor + player.getName());
            
            // Get stats and rank info
            PlayerStats stats = StatisticsManager.getStats(player);
            RankManager.Rank playerRank = RankManager.getRank(player);
            int achievements = AchievementManager.getUnlockedCount(player);
            int totalAchievements = AchievementManager.Achievement.values().length;
            
            long statValue = getStatValue(player, currentCategory);
            String valueStr = currentCategory.equals("time") ? 
                stats.formatTime(statValue) : String.valueOf(statValue);
            
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(playerRank.icon + " " + playerRank.displayName);
            lore.add("§7Achievements: §e" + achievements + "§7/§e" + totalAchievements);
            lore.add("");
            lore.add("§7" + getCategoryDisplayName(currentCategory) + ": §e" + valueStr);
            lore.add("");
            lore.add("§eClick to view stats");
            
            meta.setLore(lore);
            head.setItemMeta(meta);
        }
        
        return head;
    }

    private ItemStack createCategoryButton(String category, String name, Material material, String description) {
        boolean selected = category.equals(currentCategory);
        
        ItemBuilder builder = new ItemBuilder(material)
                .name(name + (selected ? " §a✓" : ""))
                .lore(
                    description,
                    "",
                    selected ? "§aCurrently viewing" : "§eClick to view"
                );
        
        if (selected) {
            builder.glow();
        }
        
        return builder.build();
    }

    @Override
    public void handleClick(org.bukkit.event.inventory.InventoryClickEvent event) {
        event.setCancelled(true);
        
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= 54) return;
        
        // Category buttons
        String newCategory = switch (slot) {
            case 1 -> "faps";
            case 2 -> "cumon";
            case 3 -> "cummed";
            case 4 -> "time";
            case 5 -> "bf";
            case 6 -> "rank";
            case 7 -> "achievements";
            default -> null;
        };
        
        if (newCategory != null && !newCategory.equals(currentCategory)) {
            currentCategory = newCategory;
            viewer.playSound(viewer.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            buildGUI();
            return;
        }
        
        // Player head clicks (slots 18-44)
        if (slot >= 18 && slot < 45) {
            ItemStack item = event.getCurrentItem();
            if (item != null && item.getType() == Material.PLAYER_HEAD) {
                SkullMeta meta = (SkullMeta) item.getItemMeta();
                if (meta != null && meta.getOwningPlayer() != null) {
                    Player target = meta.getOwningPlayer().getPlayer();
                    if (target != null && target.isOnline()) {
                        viewer.playSound(viewer.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                        new StatsGUI(viewer, target).open();
                    }
                }
            }
        }
        
        // Close button
        if (slot == 49) {
            viewer.closeInventory();
        }
    }

    private long getStatValue(Player player, String category) {
        PlayerStats stats = StatisticsManager.getStats(player);
        if (stats == null) return 0;
        
        return switch (category) {
            case "goons", "goon", "faps", "fap" -> stats.goonCount;
            case "cumon", "cum" -> stats.cumOnOthersCount;
            case "cummed" -> stats.gotCummedOnCount;
            case "time", "exposure" -> stats.getCurrentTotalTime();
            case "bf", "buttfinger" -> stats.buttfingersGiven;
            case "rank" -> RankManager.getRank(player).ordinal();
            case "achievements" -> AchievementManager.getUnlockedCount(player);
            default -> stats.goonCount;
        };
    }

    private String getCategoryDisplayName(String category) {
        return switch (category) {
            case "faps", "fap" -> "Faps";
            case "cumon", "cum" -> "Times Cummed on Others";
            case "cummed" -> "Times Got Cummed On";
            case "time", "exposure" -> "Exposure Time";
            case "bf", "buttfinger" -> "Buttfingers Given";
            case "rank" -> "Player Rank";
            case "achievements" -> "Achievement Count";
            default -> "Faps";
        };
    }
}
