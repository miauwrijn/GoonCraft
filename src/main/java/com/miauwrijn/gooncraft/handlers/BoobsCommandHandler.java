package com.miauwrijn.gooncraft.handlers;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import com.miauwrijn.gooncraft.Plugin;
import com.miauwrijn.gooncraft.managers.ConfigManager;
import com.miauwrijn.gooncraft.managers.GenderManager;
import com.miauwrijn.gooncraft.managers.StatisticsManager;
import com.miauwrijn.gooncraft.models.BoobModel;

/**
 * Handles /boobs commands.
 */
public class BoobsCommandHandler implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("boobs")) {
            return false;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ConfigManager.getMessage("only-players"));
            return true;
        }

        if (!GenderManager.hasBoobs(player)) {
            player.sendMessage(ConfigManager.getMessage("boobs.no-boobs"));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ConfigManager.getMessage("boobs.usage"));
            return true;
        }

        String subCommand = args[0].toLowerCase();

        return switch (subCommand) {
            case "size" -> handleSizeCommand(player, args);
            case "perkiness", "perk" -> handlePerkinessCommand(player, args);
            case "toggle" -> {
                // Redirect to /genitals command
                sender.sendMessage("§eUse §d/genitals §efor toggling visibility!");
                yield true;
            }
            case "jiggle" -> handleJiggleCommand(player);
            default -> {
                sender.sendMessage(ConfigManager.getMessage("boobs.unknown-subcommand"));
                yield true;
            }
        };
    }

    private boolean handleSizeCommand(Player player, String[] args) {
        if (args.length == 1) {
            int size = GenderManager.getBoobSize(player);
            String cupSize = BoobModel.getCupSize(size);
            player.sendMessage(ConfigManager.getMessage("boobs.your-size", "{value}", cupSize + " cup"));
            return true;
        }

        if (args[1].equalsIgnoreCase("set")) {
            if (!player.hasPermission("gooncraft.boobs.set")) {
                player.sendMessage(ConfigManager.getMessage("no-permission"));
                return true;
            }

            if (args.length < 4) {
                player.sendMessage(ConfigManager.getMessage("boobs.usage"));
                return true;
            }

            Player target = player.getServer().getPlayer(args[2]);
            if (target == null) {
                player.sendMessage(ConfigManager.getMessage("player-not-found"));
                return true;
            }

            int value;
            try {
                value = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                // Try parsing as cup size
                value = parseCupSize(args[3]);
                if (value == -1) {
                    player.sendMessage(ConfigManager.getMessage("boobs.usage"));
                    return true;
                }
            }

            if (value < BoobModel.minSize || value > BoobModel.maxSize) {
                player.sendMessage(ConfigManager.getMessage("boobs.size-range",
                    "{min}", "AA",
                    "{max}", "H"));
                return true;
            }

            GenderManager.setBoobSize(target, value);
            String cupSize = BoobModel.getCupSize(value);
            player.sendMessage(ConfigManager.getMessage("boobs.size-set",
                "{player}", target.getName(),
                "{value}", cupSize + " cup"));
            return true;
        }

        // Check other player's size
        Player target = player.getServer().getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(ConfigManager.getMessage("player-not-found"));
            return true;
        }

        if (!GenderManager.hasBoobs(target)) {
            player.sendMessage(ConfigManager.getMessage("boobs.player-no-boobs", "{player}", target.getName()));
            return true;
        }

        int size = GenderManager.getBoobSize(target);
        String cupSize = BoobModel.getCupSize(size);
        player.sendMessage(ConfigManager.getMessage("boobs.player-size",
            "{player}", target.getName(),
            "{value}", cupSize + " cup"));
        return true;
    }

    /**
     * Parse cup size string to numeric value.
     * @return numeric value (1-10) or -1 if invalid
     */
    private int parseCupSize(String input) {
        return switch (input.toUpperCase()) {
            case "AA" -> 1;
            case "A" -> 2;
            case "B" -> 3;
            case "C" -> 4;
            case "D" -> 5;
            case "DD", "E" -> 6;
            case "DDD", "F" -> 7;
            case "G" -> 8;
            case "H" -> 9;
            case "HH", "I" -> 10;
            default -> -1;
        };
    }

    private boolean handlePerkinessCommand(Player player, String[] args) {
        if (args.length == 1) {
            int perkiness = GenderManager.getBoobPerkiness(player);
            player.sendMessage(ConfigManager.getMessage("boobs.your-perkiness", "{value}", String.valueOf(perkiness)));
            return true;
        }

        if (args[1].equalsIgnoreCase("set")) {
            if (!player.hasPermission("gooncraft.boobs.set")) {
                player.sendMessage(ConfigManager.getMessage("no-permission"));
                return true;
            }

            if (args.length < 4) {
                player.sendMessage(ConfigManager.getMessage("boobs.usage"));
                return true;
            }

            Player target = player.getServer().getPlayer(args[2]);
            if (target == null) {
                player.sendMessage(ConfigManager.getMessage("player-not-found"));
                return true;
            }

            int value;
            try {
                value = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                player.sendMessage(ConfigManager.getMessage("boobs.usage"));
                return true;
            }

            if (value < BoobModel.minPerkiness || value > BoobModel.maxPerkiness) {
                player.sendMessage(ConfigManager.getMessage("boobs.perkiness-range",
                    "{min}", String.valueOf(BoobModel.minPerkiness),
                    "{max}", String.valueOf(BoobModel.maxPerkiness)));
                return true;
            }

            GenderManager.setBoobPerkiness(target, value);
            player.sendMessage(ConfigManager.getMessage("boobs.perkiness-set",
                "{player}", target.getName(),
                "{value}", String.valueOf(value)));
            return true;
        }

        // Check other player's perkiness
        Player target = player.getServer().getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(ConfigManager.getMessage("player-not-found"));
            return true;
        }

        if (!GenderManager.hasBoobs(target)) {
            player.sendMessage(ConfigManager.getMessage("boobs.player-no-boobs", "{player}", target.getName()));
            return true;
        }

        int perkiness = GenderManager.getBoobPerkiness(target);
        player.sendMessage(ConfigManager.getMessage("boobs.player-perkiness",
            "{player}", target.getName(),
            "{value}", String.valueOf(perkiness)));
        return true;
    }

    private boolean handleToggleCommand(Player player) {
        BoobModel model = GenderManager.getActiveBoobModel(player);

        if (model == null) {
            showBoobs(player);
            player.sendMessage(ConfigManager.getMessage("boobs.whipped-out"));
            
            // Track statistic for boob toggle
            StatisticsManager.incrementBoobToggleCount(player);
        } else {
            hideBoobs(player);
            player.sendMessage(ConfigManager.getMessage("boobs.put-away"));
        }
        return true;
    }

    private boolean handleJiggleCommand(Player player) {
        BoobModel model = GenderManager.getActiveBoobModel(player);

        if (model == null) {
            player.sendMessage(ConfigManager.getMessage("boobs.need-toggle"));
            return true;
        }

        model.jiggle();
        player.sendMessage(ConfigManager.getMessage("boobs.jiggle"));
        
        // Track statistic for jiggle
        StatisticsManager.incrementJiggleCount(player);
        
        return true;
    }

    private void showBoobs(Player player) {
        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        int size = GenderManager.getBoobSize(player);
        int perkiness = GenderManager.getBoobPerkiness(player);
        BoobModel boobs = new BoobModel(player, size, perkiness);
        int taskId = scheduler.scheduleSyncRepeatingTask(Plugin.instance, boobs, 0, 1L);
        GenderManager.setActiveBoobModel(player, boobs, taskId);
    }

    private void hideBoobs(Player player) {
        GenderManager.clearActiveBoobModel(player);
    }
}
