package com.miauwrijn.gooncraft.handlers;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.miauwrijn.gooncraft.gui.SkillPointsGUI;
import com.miauwrijn.gooncraft.managers.ConfigManager;
import com.miauwrijn.gooncraft.managers.SkillPointsManager;

/**
 * Handles skill points related commands.
 */
public class SkillPointsCommandHandler implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ConfigManager.getMessage("only-players"));
            return true;
        }

        String cmd = command.getName().toLowerCase();

        return switch (cmd) {
            case "skillpoints", "sp" -> handleSkillPoints(player, args);
            case "resetskillpoints" -> handleReset(player, args);
            default -> false;
        };
    }

    private boolean handleSkillPoints(Player player, String[] args) {
        // Open skill points GUI
        new SkillPointsGUI(player, player).open();
        return true;
    }

    private boolean handleReset(Player player, String[] args) {
        // Check permission
        if (!player.hasPermission("gooncraft.skillpoints.reset")) {
            player.sendMessage(ConfigManager.getMessage("no-permission"));
            return true;
        }

        // Confirm reset
        if (args.length == 0 || !args[0].equalsIgnoreCase("confirm")) {
            player.sendMessage("§c⚠️ Warning: This will reset ALL your skill points and purchased perks!");
            player.sendMessage("§7Type §e/resetskillpoints confirm §7to proceed.");
            return true;
        }

        // Reset skill points
        SkillPointsManager.resetSkillPoints(player);
        player.sendMessage("§a§l✓ Skill points and perks reset!");
        player.sendMessage("§7You can earn them back by ranking up!");
        return true;
    }
}
