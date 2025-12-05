package com.miauwrijn.gooncraft.gui;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

/**
 * Base GUI class that handles inventory creation and click events.
 */
public class GUI implements InventoryHolder {

    protected final Inventory inventory;
    protected final Map<Integer, Consumer<InventoryClickEvent>> clickHandlers = new HashMap<>();
    protected final Player viewer;

    public GUI(Player viewer, String title, int rows) {
        this.viewer = viewer;
        this.inventory = Bukkit.createInventory(this, rows * 9, title);
    }

    public void setItem(int slot, ItemStack item) {
        inventory.setItem(slot, item);
    }

    public void setItem(int slot, ItemStack item, Consumer<InventoryClickEvent> onClick) {
        inventory.setItem(slot, item);
        clickHandlers.put(slot, onClick);
    }

    public void fill(ItemStack item) {
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, item);
            }
        }
    }

    public void fillBorder(ItemStack item) {
        int size = inventory.getSize();
        int rows = size / 9;
        
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, item); // Top row
            inventory.setItem(size - 9 + i, item); // Bottom row
        }
        
        for (int row = 1; row < rows - 1; row++) {
            inventory.setItem(row * 9, item); // Left column
            inventory.setItem(row * 9 + 8, item); // Right column
        }
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        Consumer<InventoryClickEvent> handler = clickHandlers.get(event.getRawSlot());
        if (handler != null) {
            handler.accept(event);
        }
    }

    public void open() {
        viewer.openInventory(inventory);
    }

    public void close() {
        viewer.closeInventory();
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public Player getViewer() {
        return viewer;
    }

    // Utility methods for slot calculations
    public static int slot(int row, int col) {
        return (row * 9) + col;
    }

    public static int centerSlot(int row) {
        return slot(row, 4);
    }
}
