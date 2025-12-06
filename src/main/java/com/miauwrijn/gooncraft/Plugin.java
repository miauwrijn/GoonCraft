package com.miauwrijn.gooncraft;

import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.miauwrijn.gooncraft.gui.GUIListener;
import com.miauwrijn.gooncraft.handlers.BodilyFunctionsHandler;
import com.miauwrijn.gooncraft.handlers.BoobsCommandHandler;
import com.miauwrijn.gooncraft.handlers.ButtFingerCommandHandler;
import com.miauwrijn.gooncraft.handlers.GenderCommandHandler;
import com.miauwrijn.gooncraft.handlers.GenitalsCommandHandler;
import com.miauwrijn.gooncraft.handlers.PenisCommandHandler;
import com.miauwrijn.gooncraft.handlers.SkillPointsCommandHandler;
import com.miauwrijn.gooncraft.handlers.StatsCommandHandler;
import com.miauwrijn.gooncraft.managers.AchievementManager;
import com.miauwrijn.gooncraft.managers.ConfigManager;
import com.miauwrijn.gooncraft.managers.GenderManager;
import com.miauwrijn.gooncraft.managers.PenisStatisticManager;
import com.miauwrijn.gooncraft.managers.PillManager;
import com.miauwrijn.gooncraft.managers.StatisticsManager;
import com.miauwrijn.gooncraft.storage.StorageManager;

/**
 * GoonCraft - The Minecraft plugin your server never knew it needed.
 */
public class Plugin extends JavaPlugin {
    
    private static final Logger LOGGER = Logger.getLogger("GoonCraft");
    public static Plugin instance;
    
    private StatsCommandHandler statsHandler;
    private StorageManager storageManager;

    @Override
    public void onEnable() {
        instance = this;
        
        // Load configuration
        ConfigManager.load();
        
        // Initialize storage (must be before other managers!)
        storageManager = new StorageManager();
        
        // Initialize managers
        new PenisStatisticManager();
        new StatisticsManager();
        new AchievementManager();
        new GenderManager();
        new GUIListener();
        PillManager pillManager = new PillManager();
        
        // Initialize handlers
        statsHandler = new StatsCommandHandler();
        
        // Register command handlers
        getCommand("buttfinger").setExecutor(new ButtFingerCommandHandler());
        getCommand("penis").setExecutor(new PenisCommandHandler());
        getCommand("viagra").setExecutor(pillManager);
        getCommand("gooncraft").setExecutor(this::onGooncraftCommand);
        
        // Bodily functions
        BodilyFunctionsHandler bodilyHandler = new BodilyFunctionsHandler();
        getCommand("fart").setExecutor(bodilyHandler);
        getCommand("poop").setExecutor(bodilyHandler);
        getCommand("piss").setExecutor(bodilyHandler);
        
        // Gender, boobs, and genitals
        getCommand("gender").setExecutor(new GenderCommandHandler());
        getCommand("boobs").setExecutor(new BoobsCommandHandler());
        getCommand("genitals").setExecutor(new GenitalsCommandHandler());
        
        // Skill points
        SkillPointsCommandHandler skillPointsHandler = new SkillPointsCommandHandler();
        getCommand("skillpoints").setExecutor(skillPointsHandler);
        getCommand("resetskillpoints").setExecutor(skillPointsHandler);

        LOGGER.info("GoonCraft enabled! Time to get weird.");
    }

    @Override
    public void onDisable() {
        // Clean up all floating models
        PenisStatisticManager.clearAllActivePenises();
        GenderManager.clearAllActiveBoobModels();
        GenderManager.clearAllActiveVaginaModels();
        
        // Save all player data and close storage
        if (storageManager != null) {
            storageManager.shutdown();
        }
        
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
        
        // Reload storage if config changed
        if (storageManager != null) {
            storageManager.reload();
        }
        
        sender.sendMessage("§aGoonCraft config reloaded!");
        return true;
    }
    
    private void showHelp(CommandSender sender) {
        sender.sendMessage("");
        sender.sendMessage("§6§l══════ GoonCraft v" + getDescription().getVersion() + " ══════");
        sender.sendMessage("");
        sender.sendMessage("§e§lMain Commands:");
        sender.sendMessage("§e/gc stats §7[player] §8- View goon statistics");
        sender.sendMessage("§e/gc achievements §7[player] §8- View achievements");
        sender.sendMessage("§e/gc leaderboard §8- View leaderboard GUI");
        sender.sendMessage("§e/gc reload §8- Reload config §c(OP)");
        sender.sendMessage("");
        sender.sendMessage("§d§lGender & Body:");
        sender.sendMessage("§d/gender §7[change] §8- View/change gender");
        sender.sendMessage("§d/genitals §8- Toggle your genitals (gender-aware!)");
        sender.sendMessage("§d/boobs §7<size|perkiness|jiggle> §8- Boob stats/actions");
        sender.sendMessage("§d/penis §7<size|girth|bbc> §8- Penis stats");
        sender.sendMessage("");
        sender.sendMessage("§b§lActions:");
        sender.sendMessage("§b/buttfinger §7<player> §8- Buttfinger someone");
        sender.sendMessage("§b/viagra §8- Get a Viagra pill §c(OP)");
        sender.sendMessage("");
        sender.sendMessage("§a§lBodily Functions:");
        sender.sendMessage("§a/fart §8- Let one rip");
        sender.sendMessage("§a/poop §8- Drop a deuce");
        sender.sendMessage("§a/piss §8- Take a leak §7(requires /genitals)");
        sender.sendMessage("");
        sender.sendMessage("§7§oUse §e/gc help §7§ofor this menu");
        sender.sendMessage("§6§l═══════════════════════════");
    }
}
