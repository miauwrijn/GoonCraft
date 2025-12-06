package com.miauwrijn.gooncraft.gui;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;

import com.miauwrijn.gooncraft.Plugin;

/**
 * Global listener for GUI click events.
 */
public class GUIListener implements Listener {

    public GUIListener() {
        Bukkit.getPluginManager().registerEvents(this, Plugin.instance);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        
        if (holder instanceof GUI gui) {
            gui.handleClick(event);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        
        if (holder instanceof GUI) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        
        if (holder instanceof GUI gui) {
            gui.onClose();
        }
    }
}
