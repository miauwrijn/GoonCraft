package com.miauwrijn.gooncraft;

import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.miauwrijn.gooncraft.gui.GUIListener;
import com.miauwrijn.gooncraft.handlers.ButtFingerCommandHandler;
import com.miauwrijn.gooncraft.handlers.PenisCommandHandler;
import com.miauwrijn.gooncraft.handlers.StatsCommandHandler;
import com.miauwrijn.gooncraft.managers.AchievementManager;
import com.miauwrijn.gooncraft.managers.ConfigManager;
import com.miauwrijn.gooncraft.managers.PenisStatisticManager;
import com.miauwrijn.gooncraft.managers.PillManager;
import com.miauwrijn.gooncraft.managers.StatisticsManager;

/**
 * GoonCraft - The Minecraft plugin your server never knew it needed.
 */
public class Plugin extends JavaPlugin {
    
    private static final Logger LOGGER = Logger.getLogger("GoonCraft");
    public static Plugin instance;
    
    private StatsCommandHandler statsHandler;

    @Override
    public void onEnable() {
        instance = this;
        
        // Load configuration
        ConfigManager.load();
        
        // Initialize managers
        new PenisStatisticManager();
        new StatisticsManager();
        new AchievementManager();
        new GUIListener();
        PillManager pillManager = new PillManager();
        
        // Initialize handlers
        statsHandler = new StatsCommandHandler();
        
        // Register command handlers
        getCommand("buttfinger").setExecutor(new ButtFingerCommandHandler());
        getCommand("penis").setExecutor(new PenisCommandHandler());
        getCommand("viagra").setExecutor(pillManager);
        getCommand("gooncraft").setExecutor(this::onGooncraftCommand);

        LOGGER.info("GoonCraft enabled! Time to get weird.");
    }

    @Override
    public void onDisable() {
        LOGGER.info("GoonCraft disabled. Put your pants back on.");
    }

    private boolean onGooncraftCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        String[] subArgs = new String[args.length - 1];
        System.arraycopy(args, 1, subArgs, 0, args.length - 1);
        
        return switch (subCommand) {
            case "reload" -> handleReload(sender);
            case "stats" -> statsHandler.handleStats(sender, subArgs);
            case "achievements" -> statsHandler.handleAchievements(sender, subArgs);
            case "leaderboard", "lb" -> statsHandler.handleLeaderboard(sender, subArgs);
            case "help" -> {
                showHelp(sender);
                yield true;
            }
            default -> {
                sender.sendMessage("§cUnknown subcommand. Use §e/gooncraft help");
                yield true;
            }
        };
    }
    
    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("gooncraft.reload")) {
            sender.sendMessage(ConfigManager.getMessage("no-permission"));
            return true;
        }
        ConfigManager.reload();
        sender.sendMessage("§aGoonCraft config reloaded!");
        return true;
    }
    
    private void showHelp(CommandSender sender) {
        sender.sendMessage("");
        sender.sendMessage("§6§lGoonCraft §7v" + getDescription().getVersion());
        sender.sendMessage("");
        sender.sendMessage("§e/gooncraft stats §7[player] §8- View goon statistics");
        sender.sendMessage("§e/gooncraft achievements §7[player] §8- View achievements");
        sender.sendMessage("§e/gooncraft leaderboard §7[category] §8- View leaderboard");
        sender.sendMessage("§e/gooncraft reload §8- Reload config (OP)");
        sender.sendMessage("");
        sender.sendMessage("§7Other commands: §e/penis§7, §e/buttfinger§7, §e/viagra");
        sender.sendMessage("");
    }
}
