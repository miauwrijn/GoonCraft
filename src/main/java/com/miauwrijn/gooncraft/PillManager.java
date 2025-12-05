package com.miauwrijn.gooncraft;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.PluginManager;

import com.miauwrijn.gooncraft.data.PenisStatistics;

public class PillManager implements Listener, CommandExecutor {

    public PillManager() {
        PluginManager pm = Bukkit.getServer().getPluginManager();
        pm.registerEvents(this, Plugin.instance);
        CreateViagraItem();
    }

    ItemStack viagraItem;

    void CreateViagraItem() {

        viagraItem = new ItemStack(Material.GHAST_TEAR);
        // Set custom name

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GREEN + "Can't get it up or simply have a small ween, take this for a temporary boost");

        NamespacedKey key = getKey("viagra");
        ItemMeta meta = viagraItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Viagra");
            meta.setLore(lore);
            meta.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);
            viagraItem.setItemMeta(meta);
        }

        // Create a new shaped recipe
        ShapedRecipe recipe = new ShapedRecipe(key, viagraItem);

        // Define the recipe shape
        recipe.shape("DDD", "DSD", "DDD");

        // Define the ingredients
        recipe.setIngredient('D', Material.DIAMOND);
        recipe.setIngredient('S', Material.GHAST_TEAR);

        // Register the recipe
        Plugin.instance.getServer().addRecipe(recipe);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand(); // Assuming the item is held in the main hand

        NamespacedKey viagraKey = getKey("viagra");
        // Check if the item being interacted with is your custom item
        if (item != null) {
            ItemMeta meta = item.getItemMeta();
            if (meta == null)
                return;
            PersistentDataContainer container = meta.getPersistentDataContainer();
            if (!container.get(viagraKey, PersistentDataType.BOOLEAN))
                return;
            // check if player penis is toggled
            PenisStatistics stats = PenisStatisticManager.getStatistics(player);
            if (stats == null || stats.penisModel == null) {
                player.sendMessage(
                        ChatColor.RED + "You need to have your dick out of your pants in order to use this Viagra");
                return;
            }

            player.sendMessage(ChatColor.GREEN
                    + "Nice cock bro, your dick temporarily grew 5cm");
            // consume the item
            item.setAmount(item.getAmount() - 1);
            // play sound ding
            player.playSound(player.getLocation(), "entity.experience_orb.pickup", 10, 0.001f);
            // give player a buff
            int newSize = stats.viagraBoost + 5;
            PenisStatisticManager.setViagraBoost(player, newSize);
        }
    }

    private NamespacedKey getKey(String key) {
        return new NamespacedKey(Plugin.instance, key);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Command can only be excecuted as player");
            return true;
        }
        Player playerSender = (Player) sender;
        if (command.getName().equalsIgnoreCase("viagra")) {
            if (playerSender.hasPermission("gooncraft.viagra")) {
                ItemStack item = new ItemStack(viagraItem);
                playerSender.getInventory().addItem(item);
                playerSender.sendMessage(ChatColor.GREEN + "You have been given a viagra pill");
            } else {
                playerSender.sendMessage(ChatColor.RED + "You do not have permission to use this command");
            }
        }
        return true;
    }
}

