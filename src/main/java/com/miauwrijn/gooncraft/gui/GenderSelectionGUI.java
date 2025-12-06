package com.miauwrijn.gooncraft.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.miauwrijn.gooncraft.Plugin;
import com.miauwrijn.gooncraft.managers.ConfigManager;
import com.miauwrijn.gooncraft.managers.GenderManager;
import com.miauwrijn.gooncraft.managers.GenderManager.Gender;
import com.miauwrijn.gooncraft.managers.StatisticsManager;

/**
 * GUI for selecting gender on first join.
 */
public class GenderSelectionGUI extends GUI {

    private int rainbowTick = 0;
    private int taskId = -1;

    public GenderSelectionGUI(Player viewer) {
        super(viewer, "§d§lChoose Your Gender", 3);
        
        fillBorder(ItemBuilder.filler(Material.PINK_STAINED_GLASS_PANE));
        
        // Male option
        setItem(slot(1, 2), new ItemBuilder(Material.DIAMOND_SWORD)
                .name("§b§lMale")
                .lore(
                    "",
                    "§7You will have a §bpenis§7!",
                    "",
                    "§eClick to select"
                )
                .hideFlags()
                .build(),
                event -> selectGender(Gender.MALE));
        
        // Female option
        setItem(slot(1, 4), new ItemBuilder(Material.PINK_DYE)
                .name("§d§lFemale")
                .lore(
                    "",
                    "§7You will have §dboobs§7!",
                    "",
                    "§eClick to select"
                )
                .build(),
                event -> selectGender(Gender.FEMALE));
        
        // Other option (rainbow!)
        updateRainbowItem();
        
        // Start rainbow animation
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(Plugin.instance, () -> {
            rainbowTick++;
            updateRainbowItem();
        }, 0L, 5L);
        
        // Info
        setItem(slot(2, 4), new ItemBuilder(Material.PAPER)
                .name("§e§lWhy?")
                .lore(
                    "§7This determines what",
                    "§7body parts you get!",
                    "",
                    "§7You can change this later",
                    "§7with §e/gender"
                )
                .build());
        
        fill(ItemBuilder.filler());
    }

    private void updateRainbowItem() {
        String[] rainbowColors = {"§c", "§6", "§e", "§a", "§b", "§d", "§5"};
        String color = rainbowColors[rainbowTick % rainbowColors.length];
        
        // Build rainbow name
        String name = "Other";
        StringBuilder rainbowName = new StringBuilder("§l");
        for (int i = 0; i < name.length(); i++) {
            rainbowName.append(rainbowColors[(rainbowTick + i) % rainbowColors.length]);
            rainbowName.append(name.charAt(i));
        }
        
        Material[] rainbowGlass = {
            Material.RED_STAINED_GLASS,
            Material.ORANGE_STAINED_GLASS,
            Material.YELLOW_STAINED_GLASS,
            Material.LIME_STAINED_GLASS,
            Material.LIGHT_BLUE_STAINED_GLASS,
            Material.MAGENTA_STAINED_GLASS,
            Material.PURPLE_STAINED_GLASS
        };
        
        setItem(slot(1, 6), new ItemBuilder(rainbowGlass[rainbowTick % rainbowGlass.length])
                .name(rainbowName.toString())
                .lore(
                    "",
                    "§7You will have §bBOTH§7!",
                    "§d♥ §bPenis §d+ §dBoobs §d♥",
                    "",
                    "§eClick to select"
                )
                .glow()
                .build(),
                event -> selectGender(Gender.OTHER));
    }

    private void selectGender(Gender gender) {
        // Stop rainbow animation
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
        
        GenderManager.setGender(viewer, gender);
        
        // Unlock "Best of Both Worlds" achievement for selecting OTHER
        if (gender == Gender.OTHER) {
            StatisticsManager.unlockGenderOther(viewer);
        }
        
        String message = switch (gender) {
            case MALE -> ConfigManager.getMessage("gender.selected-male");
            case FEMALE -> ConfigManager.getMessage("gender.selected-female");
            case OTHER -> ConfigManager.getMessage("gender.selected-other");
        };
        
        viewer.sendMessage(message);
        viewer.playSound(viewer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        viewer.closeInventory();
    }

    @Override
    public void handleClick(org.bukkit.event.inventory.InventoryClickEvent event) {
        super.handleClick(event);
    }
}
