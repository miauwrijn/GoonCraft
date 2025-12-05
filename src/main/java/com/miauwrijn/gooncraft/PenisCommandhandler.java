package com.miauwrijn.gooncraft;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import com.miauwrijn.gooncraft.data.PenisStatistics;
import com.miauwrijn.gooncraft.models.PenisModel;

public class PenisCommandhandler implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (command.getName().equalsIgnoreCase("penis")) {

            if (args.length == 0) {
                sender.sendMessage("Command not found,, try /help kontinger");
                return true;
            } else if (handleSizeCommands(sender, command, args)) {
                return true;
            } else if (handleGirthCommands(sender, command, args)) {
                return true;
            } else if (handleBbcCommands(sender, command, args)) {
                return true;
            } else if (args[0].equalsIgnoreCase("toggle")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    PenisStatistics statistics = PenisStatisticManager.getStatistics(player);
                    if (statistics == null) {
                        player.sendMessage("§cYou need to have a penis to use this command");
                        return true;
                    }
                    if (statistics.penisModel == null) {
                        showPenis(player, statistics.bbc, statistics.size, statistics.girth, statistics.viagraBoost);
                        return true;
                    } else {
                        hidePenis(player);
                        return true;
                    }

                } else {
                    sender.sendMessage("§cOnly players can use this command");
                    return true;
                }
            }
        }
        return true;
    }

    boolean handleSizeCommands(CommandSender sender, Command command, String[] args) {
        if (args[0].equalsIgnoreCase("size")) {
            if (args.length == 1) {
                // show penis size of sender
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§cOnly players can use this command");
                    return true;
                }

                sender.sendMessage(
                        "§cYour penis size is " + PenisStatisticManager.getStatistics((Player) sender).size + "cm");
                return true;
            }

            // get first argument and check if it's equal to set
            if (args[1].equalsIgnoreCase("set")) {
                if (!sender.hasPermission("penis.size.set")) {
                    sender.sendMessage("§cYou don't have permission to do that");
                    return true;
                }
                if (args.length < 4) {
                    sender.sendMessage("§cUse: /penis size set <player> <size>");
                    return true;
                }

                Player target = sender.getServer().getPlayer(args[2]);
                if (target == null) {
                    sender.sendMessage("§cPlayer not found");
                    return true;
                }

                int size = Integer.parseInt(args[3]);
                if (size > PenisModel.maxSize || size < PenisModel.minSize) {
                    sender.sendMessage("§cPenis size should be between: " + PenisModel.minSize + "cm and "
                            + PenisModel.maxSize + "cm");
                    return true;
                }
                PenisStatisticManager.setPenisSize((Player) target, size);
                sender.sendMessage("§cYou set " + target.getDisplayName() + "'s penis size to " + size + "cm");
                return true;
            }
            Player target = sender.getServer().getPlayer(args[1]);

            if (target != null) {
                // show penis length of target to sender
                sender.sendMessage("§cPenis size of " + target.getDisplayName() + " is: "
                        + PenisStatisticManager.getStatistics((Player) target).size + "cm");
                return true;
            } else {
                sender.sendMessage("§cPlayer not found");
                return true;
            }
        }
        return false;
    }

    boolean handleGirthCommands(CommandSender sender, Command command, String[] args) {
        if (args[0].equalsIgnoreCase("girth")) {
            if (args.length == 1) {
                // show penis size of sender
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§cOnly players can use this command");
                    return true;
                }

                sender.sendMessage(
                        "§cYour penis girth is " + PenisStatisticManager.getStatistics((Player) sender).girth + "cm");
                return true;
            }

            // get first argument and check if it's equal to set
            if (args[1].equalsIgnoreCase("set")) {
                if (!sender.hasPermission("penis.girth.set")) {
                    sender.sendMessage("§cYou don't have permission to do that");
                    return true;
                }
                if (args.length < 4) {
                    sender.sendMessage("§cUse: /penis girth set <player> <girth>");
                    return true;
                }

                Player target = sender.getServer().getPlayer(args[2]);
                if (target == null) {
                    sender.sendMessage("§cPlayer not found");
                    return true;
                }

                int girth = Integer.parseInt(args[3]);
                if (girth > PenisModel.maxGirth || girth < PenisModel.minGirth) {
                    sender.sendMessage("§cPenis girth should be between: " + PenisModel.minGirth + "cm and "
                            + PenisModel.maxGirth + "cm");
                    return true;
                }
                PenisStatisticManager.setPenisGirth((Player) target, girth);
                sender.sendMessage("§cYou set " + target.getDisplayName() + "'s penis girth to " + girth + "cm");
                return true;
            }
            Player target = sender.getServer().getPlayer(args[1]);

            if (target != null) {
                // show penis length of target to sender
                sender.sendMessage("§cPenis girth of " + target.getDisplayName() + " is: "
                        + PenisStatisticManager.getStatistics((Player) target).girth + "cm");
                return true;
            } else {
                sender.sendMessage("§cPlayer not found");
                return true;
            }
        }
        return false;
    }

    boolean handleBbcCommands(CommandSender sender, Command command, String[] args) {
        if (args[0].equalsIgnoreCase("bbc")) {
            if (args.length == 1) {
                // show penis size of sender
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§cOnly players can use this command");
                    return true;
                }

                if (PenisStatisticManager.getStatistics((Player) sender).bbc) {
                    sender.sendMessage("§cYou have a BBC");
                } else {
                    sender.sendMessage("§cYou dont have a BBC");
                }
                return true;
            }

            // get first argument and check if it's equal to set
            if (args[1].equalsIgnoreCase("set")) {
                if (!sender.hasPermission("penis.bbc.set")) {
                    sender.sendMessage("§cYou don't have permission to do that");
                    return true;
                }
                if (args.length < 4) {
                    sender.sendMessage("§cUse: /penis bbc set <player> <true/false>");
                    return true;
                }

                Player target = sender.getServer().getPlayer(args[2]);
                if (target == null) {
                    sender.sendMessage("§cPlayer not found");
                    return true;
                }

                if (args[3].equalsIgnoreCase("true")) {
                    PenisStatisticManager.setPenisBbc(target, true);
                    sender.sendMessage("§cYou changed " + target.getDisplayName() + "'s penis to a bbc");

                } else if (args[3].equalsIgnoreCase("false")) {
                    PenisStatisticManager.setPenisBbc(target, false);
                    sender.sendMessage("§cYou changed " + target.getDisplayName() + "'s penis to a white penis");

                } else {
                    sender.sendMessage("Value should be either true or false");
                }
                return true;
            }
            Player target = sender.getServer().getPlayer(args[1]);

            if (target != null) {
                // show penis length of target to sender
                if (PenisStatisticManager.getStatistics((Player) target).bbc) {
                    sender.sendMessage("§c" + target.getDisplayName() + " has a BBC ");
                } else {
                    sender.sendMessage("§c" + target.getDisplayName() + " doesnt have a BBC ");
                }
                return true;
            } else {
                sender.sendMessage("§cPlayer not found");
                return true;
            }
        }
        return false;
    }

    void showPenis(Player receiver, boolean bbc, int size, int girth, int viagraBoost) {
        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();

        PenisModel penis = new PenisModel(receiver, bbc, size, girth, viagraBoost);
        int taskId = scheduler.scheduleSyncRepeatingTask(Plugin.instance, penis, 0, 0L);
        PenisStatisticManager.setActivePenis(receiver, penis, taskId);

    }

    void hidePenis(Player player) {
        PenisStatisticManager.clearActivePenis(player);
    }

}

