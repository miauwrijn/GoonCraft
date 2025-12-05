package com.miauwrijn.gooncraft;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
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

import com.miauwrijn.gooncraft.data.PenisStatistics;

public class PillManager implements Listener, CommandExecutor {

    private static final int VIAGRA_BOOST = 5;
    private final ItemStack viagraItem;
    private final NamespacedKey viagraKey;

    public PillManager() {
        this.viagraKey = new NamespacedKey(Plugin.instance, "viagra");
        this.viagraItem = createViagraItem();
        registerRecipe();
        
        Bukkit.getPluginManager().registerEvents(this, Plugin.instance);
    }

    private ItemStack createViagraItem() {
        ItemStack item = new ItemStack(Material.GHAST_TEAR);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Viagra");
            meta.setLore(List.of(
                ChatColor.GREEN + "Can't get it up or simply have a small ween?",
                ChatColor.GREEN + "Take this for a temporary +" + VIAGRA_BOOST + "cm boost!"
            ));
            meta.getPersistentDataContainer().set(viagraKey, PersistentDataType.BOOLEAN, true);
            item.setItemMeta(meta);
        }
        
        return item;
    }

    private void registerRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(viagraKey, viagraItem);
        recipe.shape("DDD", "DSD", "DDD");
        recipe.setIngredient('D', Material.DIAMOND);
        recipe.setIngredient('S', Material.GHAST_TEAR);
        
        Plugin.instance.getServer().addRecipe(recipe);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (!isViagraItem(item)) {
            return;
        }

        PenisStatistics stats = PenisStatisticManager.getStatistics(player);
        if (stats == null || stats.penisModel == null) {
            player.sendMessage(ChatColor.RED + "You need to have your dick out to use this Viagra!");
            return;
        }

        // Consume the item
        item.setAmount(item.getAmount() - 1);
        
        // Apply the boost
        PenisStatisticManager.setViagraBoost(player, stats.viagraBoost + VIAGRA_BOOST);
        
        // Effects
        player.sendMessage(ChatColor.GREEN + "Nice cock bro! Your dick temporarily grew " + VIAGRA_BOOST + "cm!");
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 0.5f);
        
        event.setCancelled(true);
    }

    private boolean isViagraItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        
        PersistentDataContainer container = meta.getPersistentDataContainer();
        Boolean isViagra = container.get(viagraKey, PersistentDataType.BOOLEAN);
        return Boolean.TRUE.equals(isViagra);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command");
            return true;
        }

        if (!command.getName().equalsIgnoreCase("viagra")) {
            return false;
        }

        if (!player.hasPermission("gooncraft.viagra")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to spawn viagra!");
            return true;
        }

        player.getInventory().addItem(new ItemStack(viagraItem));
        player.sendMessage(ChatColor.GREEN + "You've been given a Viagra pill!");
        return true;
    }
}
