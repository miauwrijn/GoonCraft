package com.miauwrijn.gooncraft.handlers;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import com.miauwrijn.gooncraft.Plugin;
import com.miauwrijn.gooncraft.PenisStatisticManager;
import com.miauwrijn.gooncraft.data.PenisStatistics;
import com.miauwrijn.gooncraft.models.PenisModel;

public class PenisCommandHandler implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("penis")) {
            return false;
        }

        if (args.length == 0) {
            sender.sendMessage("§6Usage: /penis <size|girth|bbc|toggle> [player]");
            return true;
        }

        String subCommand = args[0].toLowerCase();
        
        if (subCommand.equals("size")) {
            return handleSizeCommand(sender, args);
        } else if (subCommand.equals("girth")) {
            return handleGirthCommand(sender, args);
        } else if (subCommand.equals("bbc")) {
            return handleBbcCommand(sender, args);
        } else if (subCommand.equals("toggle")) {
            return handleToggleCommand(sender);
        } else {
            sender.sendMessage("§cUnknown subcommand. Use: size, girth, bbc, or toggle");
            return true;
        }
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
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command");
            return true;
        }
        
        Player player = (Player) sender;
        PenisStatistics stats = PenisStatisticManager.getStatistics(player);
        if (stats == null) {
            player.sendMessage("§cYou need to have a penis to use this command");
            return true;
        }

        if (stats.penisModel == null) {
            showPenis(player, stats);
            player.sendMessage("§aYou whipped it out!");
        } else {
            hidePenis(player);
            player.sendMessage("§aYou put it away.");
        }
        return true;
    }

    private boolean showOwnStat(CommandSender sender, String stat) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command");
            return true;
        }
        
        Player player = (Player) sender;
        PenisStatistics stats = PenisStatisticManager.getStatistics(player);
        if (stats == null) {
            sender.sendMessage("§cNo stats found");
            return true;
        }

        int value = stat.equals("size") ? stats.size : stats.girth;
        sender.sendMessage("§6Your penis " + stat + " is §e" + value + "cm");
        return true;
    }

    private boolean showPlayerStat(CommandSender sender, String playerName, String stat) {
        Player target = sender.getServer().getPlayer(playerName);
        if (target == null) {
            sender.sendMessage("§cPlayer not found");
            return true;
        }

        PenisStatistics stats = PenisStatisticManager.getStatistics(target);
        if (stats == null) {
            sender.sendMessage("§cNo stats found for that player");
            return true;
        }

        int value = stat.equals("size") ? stats.size : stats.girth;
        sender.sendMessage("§6" + target.getName() + "'s penis " + stat + " is §e" + value + "cm");
        return true;
    }

    private boolean setStat(CommandSender sender, String[] args, String stat) {
        String permission = "gooncraft." + stat + ".set";
        if (!sender.hasPermission(permission)) {
            sender.sendMessage("§cYou don't have permission to do that");
            return true;
        }

        if (args.length < 4) {
            sender.sendMessage("§cUsage: /penis " + stat + " set <player> <value>");
            return true;
        }

        Player target = sender.getServer().getPlayer(args[2]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found");
            return true;
        }

        int value;
        try {
            value = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid number");
            return true;
        }

        int min = stat.equals("size") ? PenisModel.minSize : PenisModel.minGirth;
        int max = stat.equals("size") ? PenisModel.maxSize : PenisModel.maxGirth;

        if (value < min || value > max) {
            sender.sendMessage("§c" + stat + " must be between " + min + " and " + max + "cm");
            return true;
        }

        if (stat.equals("size")) {
            PenisStatisticManager.setPenisSize(target, value);
        } else {
            PenisStatisticManager.setPenisGirth(target, value);
        }

        sender.sendMessage("§aSet " + target.getName() + "'s penis " + stat + " to " + value + "cm");
        return true;
    }

    private boolean showOwnBbc(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command");
            return true;
        }
        
        Player player = (Player) sender;
        PenisStatistics stats = PenisStatisticManager.getStatistics(player);
        if (stats == null) {
            sender.sendMessage("§cNo stats found");
            return true;
        }

        String status = stats.bbc ? "§aYes, you have a BBC" : "§cNo, you don't have a BBC";
        sender.sendMessage(status);
        return true;
    }

    private boolean showPlayerBbc(CommandSender sender, String playerName) {
        Player target = sender.getServer().getPlayer(playerName);
        if (target == null) {
            sender.sendMessage("§cPlayer not found");
            return true;
        }

        PenisStatistics stats = PenisStatisticManager.getStatistics(target);
        if (stats == null) {
            sender.sendMessage("§cNo stats found for that player");
            return true;
        }

        String status = stats.bbc 
            ? "§a" + target.getName() + " has a BBC" 
            : "§c" + target.getName() + " doesn't have a BBC";
        sender.sendMessage(status);
        return true;
    }

    private boolean setBbc(CommandSender sender, String[] args) {
        if (!sender.hasPermission("gooncraft.bbc.set")) {
            sender.sendMessage("§cYou don't have permission to do that");
            return true;
        }

        if (args.length < 4) {
            sender.sendMessage("§cUsage: /penis bbc set <player> <true|false>");
            return true;
        }

        Player target = sender.getServer().getPlayer(args[2]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found");
            return true;
        }

        boolean value;
        if (args[3].equalsIgnoreCase("true")) {
            value = true;
        } else if (args[3].equalsIgnoreCase("false")) {
            value = false;
        } else {
            sender.sendMessage("§cValue must be true or false");
            return true;
        }

        PenisStatisticManager.setPenisBbc(target, value);
        String status = value ? "a BBC" : "a regular penis";
        sender.sendMessage("§aSet " + target.getName() + "'s penis to " + status);
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

