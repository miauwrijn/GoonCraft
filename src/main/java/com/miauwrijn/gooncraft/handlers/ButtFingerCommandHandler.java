package com.miauwrijn.gooncraft.handlers;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.miauwrijn.gooncraft.CooldownManager;

public class ButtFingerCommandHandler implements CommandExecutor {

    private static final int COOLDOWN_SECONDS = 5;
    private static final int MAX_DISTANCE = 10;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("buttfinger")) {
            return false;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("§cUsage: /buttfinger <player>");
            return true;
        }

        Player target = player.getServer().getPlayer(args[0]);
        if (target == null) {
            player.sendMessage("§cPlayer not found");
            return true;
        }

        if (CooldownManager.hasCooldown(player, "buttfinger", COOLDOWN_SECONDS)) {
            player.sendMessage("§cYou can only buttfinger once every " + COOLDOWN_SECONDS + " seconds");
            return true;
        }

        CooldownManager.setCooldown(player, "buttfinger");

        if (target == player) {
            player.sendMessage("§6You just buttfinger'd yourself! Kinky...");
            performButtFinger(player);
            return true;
        }

        if (player.getLocation().distance(target.getLocation()) > MAX_DISTANCE) {
            player.sendMessage("§cPlayer is too far away to buttfinger!");
            return true;
        }

        target.sendMessage("§6You have been buttfinger'd by §e" + player.getName() + "§6!");
        player.sendMessage("§aYou buttfinger'd §e" + target.getName() + "§a!");
        performButtFinger(target);

        return true;
    }

    private void performButtFinger(Player target) {
        target.getWorld().playSound(
            target.getLocation(), 
            Sound.ENTITY_ITEM_FRAME_REMOVE_ITEM, 
            1.0f, 
            0.5f
        );
        
        target.getWorld().spawnParticle(
            Particle.BLOCK,
            target.getLocation().add(0, 1, 0),
            15,
            0.3, 0.3, 0.3,
            0.1,
            org.bukkit.Material.BROWN_WOOL.createBlockData()
        );
    }
}
