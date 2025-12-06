package com.miauwrijn.gooncraft;

import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.miauwrijn.gooncraft.gui.GUIListener;
import com.miauwrijn.gooncraft.gui.SkillPointsGUI;
import com.miauwrijn.gooncraft.handlers.BodilyFunctionsHandler;
import com.miauwrijn.gooncraft.handlers.BoobsCommandHandler;
import com.miauwrijn.gooncraft.handlers.ButtFingerCommandHandler;
import com.miauwrijn.gooncraft.handlers.GenderCommandHandler;
import com.miauwrijn.gooncraft.handlers.GenitalsCommandHandler;
import com.miauwrijn.gooncraft.handlers.PenisCommandHandler;
import com.miauwrijn.gooncraft.handlers.SkillPointsHandler;
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
            case "perks", "perkmanagement" -> statsHandler.handlePerks(sender, subArgs);
            case "skillpoints", "sp" -> handleSkillPoints(sender, subArgs);
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

    private boolean handleSkillPoints(CommandSender sender, String[] args) {
        if (!(sender instanceof Player viewer)) {
            sender.sendMessage(ConfigManager.getMessage("only-players"));
            return true;
        }

        // No args: open GUI for self
        if (args.length == 0) {
            new SkillPointsGUI(viewer, viewer).open();
            return true;
        }

        // Check if first arg is a player name
        Player target = getServer().getPlayer(args[0]);
        
        if (target == null) {
            sender.sendMessage("§cPlayer not found: " + args[0]);
            return true;
        }

        // One arg: open GUI for target player (if viewing others)
        if (args.length == 1) {
            new SkillPointsGUI(viewer, target).open();
            return true;
        }

        // Admin commands: /gc skillpoints <username> <set/add/remove> <count>
        if (!viewer.hasPermission("gooncraft.skillpoints.admin")) {
            sender.sendMessage(ConfigManager.getMessage("no-permission"));
            return true;
        }

        String action = args[1].toLowerCase();
        
        if (!action.equals("set") && !action.equals("add") && !action.equals("remove")) {
            sender.sendMessage("§cInvalid action. Use: set, add, or remove");
            sender.sendMessage("§7Usage: /gc skillpoints <player> <set/add/remove> <amount>");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage("§cUsage: /gc skillpoints <player> <set/add/remove> <amount>");
            return true;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[2]);
            if (amount < 0) {
                sender.sendMessage("§cAmount must be positive!");
                return true;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid number: " + args[2]);
            return true;
        }

        switch (action) {
            case "set":
                SkillPointsHandler.setSkillPoints(target, amount);
                sender.sendMessage("§a§l✓ Set " + target.getName() + "'s skill points to " + amount);
                if (target.isOnline()) {
                    target.sendMessage("§6Your skill points have been set to " + amount + " by " + viewer.getName());
                }
                break;
            case "add":
                SkillPointsHandler.addSkillPoints(target, amount);
                sender.sendMessage("§a§l✓ Added " + amount + " skill points to " + target.getName());
                if (target.isOnline()) {
                    target.sendMessage("§6§l+ " + amount + " Skill Point" + (amount > 1 ? "s" : "") + "! §7(Given by " + viewer.getName() + ")");
                }
                break;
            case "remove":
                int currentPoints = SkillPointsHandler.getSkillPoints(target);
                SkillPointsHandler.removeSkillPoints(target, amount);
                sender.sendMessage("§a§l✓ Removed " + amount + " skill points from " + target.getName() + " (had " + currentPoints + ")");
                if (target.isOnline()) {
                    target.sendMessage("§c§l- " + amount + " Skill Point" + (amount > 1 ? "s" : "") + " §7(Removed by " + viewer.getName() + ")");
                }
                break;
        }
        
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
        sender.sendMessage("§e/gc perks §7[player] §8- Manage rank perks");
        sender.sendMessage("§e/gc skillpoints §7[player] §8- View skill points shop");
        sender.sendMessage("§e/gc skillpoints §7<player> <set/add/remove> <count> §8- Admin skill points §c(OP)");
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
