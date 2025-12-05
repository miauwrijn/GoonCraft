package com.miauwrijn.gooncraft.handlers;

import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.miauwrijn.gooncraft.managers.ConfigManager;
import com.miauwrijn.gooncraft.managers.CooldownManager;
import com.miauwrijn.gooncraft.managers.StatisticsManager;

public class ButtFingerCommandHandler implements CommandExecutor {

    private static final double MAX_DISTANCE = 5.0;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("buttfinger")) {
            return false;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ConfigManager.getMessage("only-players"));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ConfigManager.getMessage("buttfinger.usage"));
            return true;
        }

        int cooldownSeconds = ConfigManager.getButtfingerCooldown();
        if (CooldownManager.hasCooldown(player, "buttfinger", cooldownSeconds)) {
            player.sendMessage(ConfigManager.getMessage("buttfinger.cooldown", 
                "{value}", String.valueOf(cooldownSeconds)));
            return true;
        }

        Player target = sender.getServer().getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ConfigManager.getMessage("player-not-found"));
            return true;
        }

        if (target.equals(player)) {
            buttfinger(player, player, true);
            return true;
        }

        if (player.getLocation().distance(target.getLocation()) > MAX_DISTANCE) {
            player.sendMessage(ConfigManager.getMessage("buttfinger.too-far"));
            return true;
        }

        buttfinger(player, target, false);
        return true;
    }

    private void buttfinger(Player executor, Player target, boolean isSelf) {
        CooldownManager.setCooldown(executor, "buttfinger");

        // Track statistics (with target for unique player tracking)
        if (isSelf) {
            StatisticsManager.incrementButtfingersGiven(executor);
        } else {
            StatisticsManager.incrementButtfingersGiven(executor, target);
            StatisticsManager.incrementButtfingersReceived(target);
        }

        if (isSelf) {
            executor.sendMessage(ConfigManager.getMessage("buttfinger.self"));
            executor.playSound(executor.getLocation(), Sound.BLOCK_SLIME_BLOCK_PLACE, 1.0f, 0.5f);
        } else {
            executor.sendMessage(ConfigManager.getMessage("buttfinger.success", 
                "{target}", target.getName()));
            target.sendMessage(ConfigManager.getMessage("buttfinger.victim", 
                "{player}", executor.getName()));
            target.playSound(target.getLocation(), Sound.BLOCK_SLIME_BLOCK_PLACE, 1.0f, 0.5f);
        }
    }
}
