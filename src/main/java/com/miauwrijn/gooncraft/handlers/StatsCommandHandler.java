package com.miauwrijn.gooncraft.handlers;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.miauwrijn.gooncraft.gui.AchievementsGUI;
import com.miauwrijn.gooncraft.gui.LeaderboardGUI;
import com.miauwrijn.gooncraft.gui.StatsGUI;
import com.miauwrijn.gooncraft.managers.ConfigManager;

/**
 * Handles /gooncraft stats, achievements, and leaderboard subcommands.
 */
public class StatsCommandHandler {

    public boolean handleStats(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ConfigManager.getMessage("only-players"));
            return true;
        }
        
        Player target = player;
        
        if (args.length > 0) {
            target = player.getServer().getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(ConfigManager.getMessage("player-not-found"));
                return true;
            }
        }
        
        // Open GUI
        new StatsGUI(player, target).open();
        return true;
    }

    public boolean handleAchievements(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ConfigManager.getMessage("only-players"));
            return true;
        }
        
        Player target = player;
        
        if (args.length > 0) {
            target = player.getServer().getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(ConfigManager.getMessage("player-not-found"));
                return true;
            }
        }
        
        // Open GUI
        new AchievementsGUI(player, target).open();
        return true;
    }

    public boolean handleLeaderboard(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ConfigManager.getMessage("only-players"));
            return true;
        }
        
        // Open GUI
        new LeaderboardGUI(player).open();
        return true;
    }
}
