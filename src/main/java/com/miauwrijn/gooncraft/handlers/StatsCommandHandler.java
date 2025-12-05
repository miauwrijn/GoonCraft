package com.miauwrijn.gooncraft.handlers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.miauwrijn.gooncraft.data.PlayerStats;
import com.miauwrijn.gooncraft.gui.AchievementsGUI;
import com.miauwrijn.gooncraft.gui.StatsGUI;
import com.miauwrijn.gooncraft.managers.ConfigManager;
import com.miauwrijn.gooncraft.managers.StatisticsManager;

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
        
        String category = args.length > 0 ? args[0].toLowerCase() : "faps";
        
        player.sendMessage("");
        player.sendMessage(ConfigManager.getMessage("leaderboard.header", 
            "{category}", getCategoryDisplayName(category)));
        player.sendMessage("");
        
        // Get online players sorted by stat
        List<Player> onlinePlayers = new ArrayList<>(player.getServer().getOnlinePlayers());
        
        onlinePlayers.sort((a, b) -> {
            PlayerStats statsA = StatisticsManager.getStats(a);
            PlayerStats statsB = StatisticsManager.getStats(b);
            return Long.compare(getStatValue(statsB, category), getStatValue(statsA, category));
        });
        
        int rank = 1;
        for (Player p : onlinePlayers) {
            if (rank > 10) break;
            
            PlayerStats stats = StatisticsManager.getStats(p);
            long value = getStatValue(stats, category);
            
            String valueStr = category.equals("time") ? stats.formatTime(value) : String.valueOf(value);
            
            String color = rank <= 3 ? "§6" : "§7";
            String medal = switch (rank) {
                case 1 -> "§6🥇 ";
                case 2 -> "§7🥈 ";
                case 3 -> "§c🥉 ";
                default -> "§7#" + rank + " ";
            };
            
            player.sendMessage(medal + "§f" + p.getName() + " §7- §e" + valueStr);
            rank++;
        }
        
        if (onlinePlayers.isEmpty()) {
            player.sendMessage("§7No players online.");
        }
        
        player.sendMessage("");
        player.sendMessage("§7Categories: §efaps§7, §ecumon§7, §ecummed§7, §etime§7, §ebf");
        player.sendMessage("");
        return true;
    }

    private long getStatValue(PlayerStats stats, String category) {
        return switch (category) {
            case "faps", "fap" -> stats.fapCount;
            case "cumon", "cum" -> stats.cumOnOthersCount;
            case "cummed" -> stats.gotCummedOnCount;
            case "time", "exposure" -> stats.getCurrentTotalTime();
            case "bf", "buttfinger" -> stats.buttfingersGiven;
            default -> stats.fapCount;
        };
    }

    private String getCategoryDisplayName(String category) {
        return switch (category) {
            case "faps", "fap" -> "Faps";
            case "cumon", "cum" -> "Times Cummed on Others";
            case "cummed" -> "Times Got Cummed On";
            case "time", "exposure" -> "Time with Penis Out";
            case "bf", "buttfinger" -> "Buttfingers Given";
            default -> "Faps";
        };
    }
}
