package com.miauwrijn.gooncraft.handlers;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import com.miauwrijn.gooncraft.Plugin;
import com.miauwrijn.gooncraft.data.PenisStatistics;
import com.miauwrijn.gooncraft.managers.ConfigManager;
import com.miauwrijn.gooncraft.managers.PenisStatisticManager;
import com.miauwrijn.gooncraft.managers.StatisticsManager;
import com.miauwrijn.gooncraft.models.PenisModel;

public class PenisCommandHandler implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("penis")) {
            return false;
        }

        if (args.length == 0) {
            sender.sendMessage(ConfigManager.getMessage("penis.usage"));
            return true;
        }

        String subCommand = args[0].toLowerCase();
        
        return switch (subCommand) {
            case "size" -> handleSizeCommand(sender, args);
            case "girth" -> handleGirthCommand(sender, args);
            case "bbc" -> handleBbcCommand(sender, args);
            case "toggle" -> handleToggleCommand(sender);
            default -> {
                sender.sendMessage(ConfigManager.getMessage("penis.unknown-subcommand"));
                yield true;
            }
        };
    }

    private boolean handleSizeCommand(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return showOwnStat(sender, "size");
        }
        if (args[1].equalsIgnoreCase("set")) {
            return setStat(sender, args, "size");
        }
        return showPlayerStat(sender, args[1], "size");
    }

    private boolean handleGirthCommand(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return showOwnStat(sender, "girth");
        }
        if (args[1].equalsIgnoreCase("set")) {
            return setStat(sender, args, "girth");
        }
        return showPlayerStat(sender, args[1], "girth");
    }

    private boolean handleBbcCommand(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return showOwnBbc(sender);
        }
        if (args[1].equalsIgnoreCase("set")) {
            return setBbc(sender, args);
        }
        return showPlayerBbc(sender, args[1]);
    }

    private boolean handleToggleCommand(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ConfigManager.getMessage("only-players"));
            return true;
        }

        PenisStatistics stats = PenisStatisticManager.getStatistics(player);
        if (stats == null) {
            player.sendMessage(ConfigManager.getMessage("penis.need-penis"));
            return true;
        }

        if (stats.penisModel == null) {
            showPenis(player, stats);
            player.sendMessage(ConfigManager.getMessage("penis.whipped-out"));
            StatisticsManager.startPenisOutTimer(player);
        } else {
            hidePenis(player);
            player.sendMessage(ConfigManager.getMessage("penis.put-away"));
            StatisticsManager.stopPenisOutTimer(player);
        }
        return true;
    }

    private boolean showOwnStat(CommandSender sender, String stat) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ConfigManager.getMessage("only-players"));
            return true;
        }

        PenisStatistics stats = PenisStatisticManager.getStatistics(player);
        if (stats == null) {
            sender.sendMessage(ConfigManager.getMessage("no-stats"));
            return true;
        }

        int value = stat.equals("size") ? stats.size : stats.girth;
        String messageKey = stat.equals("size") ? "penis.your-size" : "penis.your-girth";
        sender.sendMessage(ConfigManager.getMessage(messageKey, "{value}", String.valueOf(value)));
        return true;
    }

    private boolean showPlayerStat(CommandSender sender, String playerName, String stat) {
        Player target = sender.getServer().getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(ConfigManager.getMessage("player-not-found"));
            return true;
        }

        PenisStatistics stats = PenisStatisticManager.getStatistics(target);
        if (stats == null) {
            sender.sendMessage(ConfigManager.getMessage("no-stats"));
            return true;
        }

        int value = stat.equals("size") ? stats.size : stats.girth;
        String messageKey = stat.equals("size") ? "penis.player-size" : "penis.player-girth";
        sender.sendMessage(ConfigManager.getMessage(messageKey, 
            "{player}", target.getName(), 
            "{value}", String.valueOf(value)));
        return true;
    }

    private boolean setStat(CommandSender sender, String[] args, String stat) {
        String permission = "gooncraft." + stat + ".set";
        if (!sender.hasPermission(permission)) {
            sender.sendMessage(ConfigManager.getMessage("no-permission"));
            return true;
        }

        if (args.length < 4) {
            sender.sendMessage(ConfigManager.getMessage("penis.usage"));
            return true;
        }

        Player target = sender.getServer().getPlayer(args[2]);
        if (target == null) {
            sender.sendMessage(ConfigManager.getMessage("player-not-found"));
            return true;
        }

        int value;
        try {
            value = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ConfigManager.getMessage("penis.usage"));
            return true;
        }

        int min = stat.equals("size") ? PenisModel.minSize : PenisModel.minGirth;
        int max = stat.equals("size") ? PenisModel.maxSize : PenisModel.maxGirth;

        if (value < min || value > max) {
            String rangeKey = stat.equals("size") ? "penis.size-range" : "penis.girth-range";
            sender.sendMessage(ConfigManager.getMessage(rangeKey, 
                "{min}", String.valueOf(min), 
                "{max}", String.valueOf(max)));
            return true;
        }

        if (stat.equals("size")) {
            PenisStatisticManager.setPenisSize(target, value);
        } else {
            PenisStatisticManager.setPenisGirth(target, value);
        }

        String setKey = stat.equals("size") ? "penis.size-set" : "penis.girth-set";
        sender.sendMessage(ConfigManager.getMessage(setKey, 
            "{player}", target.getName(), 
            "{value}", String.valueOf(value)));
        return true;
    }

    private boolean showOwnBbc(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ConfigManager.getMessage("only-players"));
            return true;
        }

        PenisStatistics stats = PenisStatisticManager.getStatistics(player);
        if (stats == null) {
            sender.sendMessage(ConfigManager.getMessage("no-stats"));
            return true;
        }

        String messageKey = stats.bbc ? "penis.you-have-bbc" : "penis.you-no-bbc";
        sender.sendMessage(ConfigManager.getMessage(messageKey));
        return true;
    }

    private boolean showPlayerBbc(CommandSender sender, String playerName) {
        Player target = sender.getServer().getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(ConfigManager.getMessage("player-not-found"));
            return true;
        }

        PenisStatistics stats = PenisStatisticManager.getStatistics(target);
        if (stats == null) {
            sender.sendMessage(ConfigManager.getMessage("no-stats"));
            return true;
        }

        String messageKey = stats.bbc ? "penis.player-has-bbc" : "penis.player-no-bbc";
        sender.sendMessage(ConfigManager.getMessage(messageKey, "{player}", target.getName()));
        return true;
    }

    private boolean setBbc(CommandSender sender, String[] args) {
        if (!sender.hasPermission("gooncraft.bbc.set")) {
            sender.sendMessage(ConfigManager.getMessage("no-permission"));
            return true;
        }

        if (args.length < 4) {
            sender.sendMessage(ConfigManager.getMessage("penis.usage"));
            return true;
        }

        Player target = sender.getServer().getPlayer(args[2]);
        if (target == null) {
            sender.sendMessage(ConfigManager.getMessage("player-not-found"));
            return true;
        }

        boolean value;
        if (args[3].equalsIgnoreCase("true")) {
            value = true;
        } else if (args[3].equalsIgnoreCase("false")) {
            value = false;
        } else {
            sender.sendMessage(ConfigManager.getMessage("penis.invalid-boolean"));
            return true;
        }

        PenisStatisticManager.setPenisBbc(target, value);
        String messageKey = value ? "penis.bbc-set-true" : "penis.bbc-set-false";
        sender.sendMessage(ConfigManager.getMessage(messageKey, "{player}", target.getName()));
        return true;
    }

    private void showPenis(Player player, PenisStatistics stats) {
        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        PenisModel penis = new PenisModel(player, stats.bbc, stats.size, stats.girth, stats.viagraBoost);
        int taskId = scheduler.scheduleSyncRepeatingTask(Plugin.instance, penis, 0, 1L);
        PenisStatisticManager.setActivePenis(player, penis, taskId);
    }

    private void hidePenis(Player player) {
        PenisStatisticManager.clearActivePenis(player);
    }
}
