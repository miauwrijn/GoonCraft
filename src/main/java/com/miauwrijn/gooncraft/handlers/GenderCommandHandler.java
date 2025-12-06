package com.miauwrijn.gooncraft.handlers;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.miauwrijn.gooncraft.gui.GenderSelectionGUI;
import com.miauwrijn.gooncraft.managers.ConfigManager;
import com.miauwrijn.gooncraft.managers.GenderManager;
import com.miauwrijn.gooncraft.managers.GenderManager.Gender;
import com.miauwrijn.gooncraft.managers.StatisticsManager;

/**
 * Handles /gender command to view or change gender.
 */
public class GenderCommandHandler implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ConfigManager.getMessage("only-players"));
            return true;
        }

        if (args.length == 0) {
            // Show current gender or open selection
            Gender gender = GenderManager.getGender(player);
            if (gender == null) {
                new GenderSelectionGUI(player).open();
            } else {
                String genderName = switch (gender) {
                    case MALE -> "§bMale";
                    case FEMALE -> "§dFemale";
                    case OTHER -> "§c§lO§6§lt§e§lh§a§le§b§lr";
                };
                player.sendMessage(ConfigManager.getMessage("gender.current", "{gender}", genderName));
                player.sendMessage(ConfigManager.getMessage("gender.change-hint"));
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("change") || args[0].equalsIgnoreCase("set")) {
            // Clear active models before changing
            GenderManager.clearActiveBoobModel(player);
            com.miauwrijn.gooncraft.managers.PenisStatisticManager.clearActivePenis(player);
            
            // Track gender change statistic
            StatisticsManager.incrementGenderChanges(player);
            
            new GenderSelectionGUI(player).open();
            return true;
        }

        player.sendMessage("§6Usage: §e/gender §7- View gender");
        player.sendMessage("§6Usage: §e/gender change §7- Change gender");
        return true;
    }
}
