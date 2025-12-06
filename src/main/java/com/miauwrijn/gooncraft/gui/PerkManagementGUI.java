package com.miauwrijn.gooncraft.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.miauwrijn.gooncraft.managers.RankManager;
import com.miauwrijn.gooncraft.ranks.BaseRank;
import com.miauwrijn.gooncraft.storage.PlayerData;
import com.miauwrijn.gooncraft.storage.StorageManager;

/**
 * GUI for managing and toggling rank perks on/off.
 */
public class PerkManagementGUI extends GUI {

    private final Player target;
    private int page = 0;
    private static final int PERKS_PER_PAGE = 28;

    public PerkManagementGUI(Player viewer, Player target) {
        super(viewer, "§6§l✦ Perk Management ✦", 6);
        this.target = target;
        render();
    }

    private void render() {
        inventory.clear();
        clickHandlers.clear();
        
        BaseRank currentRank = RankManager.getRank(target);
        PlayerData data = StorageManager.getPlayerData(target);
        Set<String> disabledPerks = data.disabledPerks;
        
        // Top border
        fillBorder(ItemBuilder.filler(Material.ORANGE_STAINED_GLASS_PANE));
        
        // Info header
        setItem(slot(0, 4), new ItemBuilder(Material.BOOK)
                .name("§6§lPerk Management")
                .lore(
                    "",
                    "§7Current Rank: " + currentRank.getDisplayName(),
                    "§7Total Perks Available: §e" + getTotalPerksForRank(currentRank),
                    "",
                    "§8Toggle perks on/off by clicking them",
                    "§8Disabled perks won't have any effect"
                )
                .build());
        
        // Get all perks for current rank and below
        List<PerkInfo> allPerks = getAllPerksForRank(currentRank);
        int startIndex = page * PERKS_PER_PAGE;
        
        // Display perks
        int slotIndex = 10;
        for (int i = startIndex; i < Math.min(startIndex + PERKS_PER_PAGE, allPerks.size()); i++) {
            PerkInfo perk = allPerks.get(i);
            boolean isEnabled = !disabledPerks.contains(perk.id);
            
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§7" + perk.description);
            lore.add("");
            lore.add("§7From Rank: " + perk.rank.getDisplayName());
            lore.add("");
            
            if (isEnabled) {
                lore.add("§a§l✓ ENABLED");
                lore.add("§7Click to disable");
            } else {
                lore.add("§c§l✗ DISABLED");
                lore.add("§7Click to enable");
            }
            
            Material material = isEnabled ? Material.EMERALD : Material.REDSTONE_BLOCK;
            if (perk.hasEffect) {
                material = isEnabled ? Material.LIME_DYE : Material.GRAY_DYE;
            }
            
            ItemBuilder builder = new ItemBuilder(material)
                    .name((isEnabled ? "§a" : "§c") + perk.icon + " §f" + perk.name)
                    .lore(lore);
            
            if (isEnabled) {
                builder.glow();
            }
            
            final PerkInfo finalPerk = perk;
            final boolean finalEnabled = isEnabled;
            setItem(slotIndex, builder.build(), event -> {
                togglePerk(target, finalPerk, !finalEnabled);
                render(); // Refresh GUI
            });
            
            slotIndex++;
            // Skip border slots
            if ((slotIndex + 1) % 9 == 0) {
                slotIndex += 2;
            }
            if (slotIndex >= 44) break;
        }
        
        // Fill empty slots
        for (int row = 1; row <= 4; row++) {
            for (int col = 1; col <= 7; col++) {
                int s = slot(row, col);
                if (inventory.getItem(s) == null) {
                    setItem(s, ItemBuilder.filler(Material.BLACK_STAINED_GLASS_PANE));
                }
            }
        }
        
        // Bottom navigation
        int totalPages = (int) Math.ceil(allPerks.size() / (double) PERKS_PER_PAGE);
        
        // Back button
        setItem(slot(5, 0), new ItemBuilder(Material.ARROW)
                .name("§c§l← Back")
                .lore("§7Return to stats")
                .build(),
                event -> new StatsGUI(viewer, target).open());
        
        // Previous page
        if (page > 0) {
            setItem(slot(5, 3), new ItemBuilder(Material.ARROW)
                    .name("§e§lPrevious Page")
                    .lore("§7Page " + page + "/" + (totalPages - 1))
                    .build(),
                    event -> {
                        page--;
                        render();
                    });
        }
        
        // Page indicator
        setItem(slot(5, 4), new ItemBuilder(Material.BOOK)
                .name("§6§lPage " + (page + 1) + "/" + Math.max(1, totalPages))
                .lore(
                    "",
                    "§7Total Perks: §e" + allPerks.size(),
                    "",
                    "§8Click perks to toggle"
                )
                .build());
        
        // Next page
        if ((page + 1) * PERKS_PER_PAGE < allPerks.size()) {
            setItem(slot(5, 5), new ItemBuilder(Material.ARROW)
                    .name("§e§lNext Page")
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
    
    private List<PerkInfo> getAllPerksForRank(BaseRank rank) {
        List<PerkInfo> perks = new ArrayList<>();
        BaseRank[] allRanks = RankManager.getAllRanks();
        
        Map<String, PerkInfo> perkMap = new HashMap<>();
        int genericPerkCounter = 0;
        
        for (BaseRank r : allRanks) {
            if (r.getOrdinal() <= rank.getOrdinal()) {
                for (String perkText : r.getPerkDescriptions()) {
                    String perkId = "perk_" + r.getOrdinal() + "_" + genericPerkCounter;
                    if (!perkMap.containsKey(perkId)) {
                        PerkInfo info = new PerkInfo(perkId, "⭐", perkText, perkText, r, true);
                        perkMap.put(perkId, info);
                        perks.add(info);
                        genericPerkCounter++;
                    }
                }
            }
        }
        
        return perks;
    }
    
    private int getTotalPerksForRank(BaseRank rank) {
        return getAllPerksForRank(rank).size();
    }
    
    private void togglePerk(Player player, PerkInfo perk, boolean enable) {
        PlayerData data = StorageManager.getPlayerData(player);
        String displayName = perk.icon + " " + perk.name;
        if (enable) {
            data.disabledPerks.remove(perk.id);
            player.sendMessage("§a§l✓ Enabled perk: " + displayName);
        } else {
            data.disabledPerks.add(perk.id);
            player.sendMessage("§c§l✗ Disabled perk: " + displayName);
        }
        StorageManager.savePlayerData(player.getUniqueId());
    }
    
    private static class PerkInfo {
        final String id;
        final String icon;
        final String name;
        final String description;
        final BaseRank rank;
        final boolean hasEffect;
        
        PerkInfo(String id, String icon, String name, String description, BaseRank rank, boolean hasEffect) {
            this.id = id;
            this.icon = icon;
            this.name = name;
            this.description = description;
            this.rank = rank;
            this.hasEffect = hasEffect;
        }
    }
}

