package com.miauwrijn.gooncraft.managers;

import org.bukkit.Bukkit;
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

import com.miauwrijn.gooncraft.Plugin;
import com.miauwrijn.gooncraft.data.PenisStatistics;
import com.miauwrijn.gooncraft.managers.StatisticsManager;

public class PillManager implements Listener, CommandExecutor {

    private final NamespacedKey viagraKey;

    public PillManager() {
        this.viagraKey = new NamespacedKey(Plugin.instance, "viagra");
        registerRecipe();
        
        Bukkit.getPluginManager().registerEvents(this, Plugin.instance);
    }

    private ItemStack createViagraItem() {
        ItemStack item = new ItemStack(Material.GHAST_TEAR);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(ConfigManager.getViagraName());
            meta.setLore(ConfigManager.getViagraLore());
            meta.getPersistentDataContainer().set(viagraKey, PersistentDataType.BOOLEAN, true);
            item.setItemMeta(meta);
        }
        
        return item;
    }

    private void registerRecipe() {
        ItemStack viagraItem = createViagraItem();
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
            player.sendMessage(ConfigManager.getMessage("viagra.need-toggle"));
            return;
        }

        // Consume the item
        item.setAmount(item.getAmount() - 1);
        
        // Apply the boost
        int boost = ConfigManager.getViagraBoost();
        PenisStatisticManager.setViagraBoost(player, stats.viagraBoost + boost);
        
        // Track statistic
        StatisticsManager.incrementViagraUsed(player);
        
        // Effects
        player.sendMessage(ConfigManager.getMessage("viagra.success", "{value}", String.valueOf(boost)));
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
            sender.sendMessage(ConfigManager.getMessage("only-players"));
            return true;
        }

        if (!command.getName().equalsIgnoreCase("viagra")) {
            return false;
        }

        if (!player.hasPermission("gooncraft.viagra")) {
            player.sendMessage(ConfigManager.getMessage("no-permission"));
            return true;
        }

        player.getInventory().addItem(createViagraItem());
        player.sendMessage(ConfigManager.getMessage("viagra.given"));
        return true;
    }
}
