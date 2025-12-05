package com.miauwrijn.gooncraft;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KontvingerCommandHandler implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (command.getName().equalsIgnoreCase("kontvinger")) {

            if (!(sender instanceof Player)) {
                sender.sendMessage("§cOnly players can use this command");
                return true;
            }

            Player senderPlayer = (Player) sender;
            if (args.length == 0) {
                senderPlayer.sendMessage("§cUse: /kontvinger <player>");
                return true;
            } else {
                // find player with name args[0]
                Player player = senderPlayer.getServer().getPlayer(args[0]);
                if (player != null) {

                    if (CooldownManager.hasCooldown(senderPlayer, "kontvinger", 5)) {
                        senderPlayer.sendMessage("§cYou can only kontvinger once every 5 seconds");
                        return true;
                    }

                    CooldownManager.setCooldown(senderPlayer, "kontvinger");

                    // check if player is near sender
                    if (player == senderPlayer) {
                        senderPlayer.sendMessage("§cYou just kontvinger'd yourself!");
                        // play sound item frame remove item at player location to player
                        kontvinger(player);
                        return true;
                    }

                    if (senderPlayer.getLocation().distance(player.getLocation()) > 10) {
                        sender.sendMessage("§cPlayer is too far away to kontvinger!");
                        return true;
                    }

                    // if player is found, send message to player
                    player.sendMessage("§cYou have been kontvinger'd by " + senderPlayer.getName());
                    kontvinger(player);

                    return true;
                } else {
                    senderPlayer.sendMessage("§cPlayer not found");
                    return true;
                }
            }
        }

        return true;
    }

    void kontvinger(Player receiver) {
        receiver.getWorld().playSound(receiver.getLocation(), org.bukkit.Sound.ENTITY_ITEM_FRAME_REMOVE_ITEM, 10, 10);
        // spawn brown particle at player location
        receiver.getWorld().spawnParticle(org.bukkit.Particle.BLOCK_DUST, receiver.getLocation(), 10, 0.5, 0.5, 0.5,
                0.1, org.bukkit.Material.BROWN_WOOL.createBlockData());

    }

}
