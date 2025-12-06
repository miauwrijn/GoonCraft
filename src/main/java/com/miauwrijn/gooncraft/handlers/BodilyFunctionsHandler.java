package com.miauwrijn.gooncraft.handlers;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.miauwrijn.gooncraft.Plugin;
import com.miauwrijn.gooncraft.managers.ConfigManager;
import com.miauwrijn.gooncraft.managers.CooldownManager;
import com.miauwrijn.gooncraft.managers.GenderManager;
import com.miauwrijn.gooncraft.managers.StatisticsManager;
import com.miauwrijn.gooncraft.util.AnimalInteractionHandler;

/**
 * Handles /fart, /poop, and /piss commands.
 */
public class BodilyFunctionsHandler implements CommandExecutor {

    private static final int FART_COOLDOWN = 3;
    private static final int POOP_COOLDOWN = 10;
    private static final int PISS_COOLDOWN = 5;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ConfigManager.getMessage("only-players"));
            return true;
        }

        String cmd = command.getName().toLowerCase();

        return switch (cmd) {
            case "fart" -> handleFart(player);
            case "poop" -> handlePoop(player);
            case "piss" -> handlePiss(player);
            default -> false;
        };
    }

    private boolean handleFart(Player player) {
        if (CooldownManager.hasCooldown(player, "fart", FART_COOLDOWN)) {
            player.sendMessage(ConfigManager.getMessage("bodily.cooldown"));
            return true;
        }

        CooldownManager.setCooldown(player, "fart");
        
        // Track statistic
        StatisticsManager.incrementFartCount(player);

        Location loc = player.getLocation().add(0, 0.5, 0);
        
        // Particles behind the player
        Vector behind = player.getLocation().getDirection().multiply(-0.5);
        Location particleLoc = loc.clone().add(behind);

        // Green/brown cloud particles
        player.getWorld().spawnParticle(Particle.SMOKE, particleLoc, 15, 0.2, 0.1, 0.2, 0.02);
        // Green dust for the fart cloud
        DustOptions fartDust = new DustOptions(Color.fromRGB(139, 119, 42), 1.5f);
        player.getWorld().spawnParticle(Particle.DUST, particleLoc, 10, 0.3, 0.1, 0.3, 0, fartDust);

        // Fart sound
        player.getWorld().playSound(loc, Sound.ENTITY_CREEPER_HURT, 0.8f, 0.5f);

        // Broadcast message
        if (ConfigManager.showFartMessages()) {
            String message = ConfigManager.getMessage("bodily.fart", "{player}", player.getName());
            for (Player nearby : player.getWorld().getPlayers()) {
                if (nearby.getLocation().distance(player.getLocation()) < 15) {
                    nearby.sendMessage(message);
                }
            }
        }

        return true;
    }

    private boolean handlePoop(Player player) {
        if (CooldownManager.hasCooldown(player, "poop", POOP_COOLDOWN)) {
            player.sendMessage(ConfigManager.getMessage("bodily.cooldown"));
            return true;
        }

        CooldownManager.setCooldown(player, "poop");
        
        // Track statistic
        StatisticsManager.incrementPoopCount(player);

        Location loc = player.getLocation();

        // Play straining sound
        player.getWorld().playSound(loc, Sound.ENTITY_VILLAGER_HURT, 1.0f, 0.3f);

        // Delayed poop effect
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= 20) {
                    // Final plop
                    Location poopLoc = player.getLocation().add(0, 0.3, 0);
                    Vector behind = player.getLocation().getDirection().multiply(-0.3);
                    poopLoc.add(behind);

                    // Brown particles falling
                    player.getWorld().spawnParticle(Particle.FALLING_DRIPSTONE_LAVA, poopLoc, 8, 0.1, 0.1, 0.1, 0);
                    player.getWorld().spawnParticle(Particle.SMOKE, poopLoc.clone().add(0, -0.5, 0), 5, 0.1, 0.05, 0.1, 0.01);
                    
                    // Plop sound
                    player.getWorld().playSound(poopLoc, Sound.ENTITY_SLIME_SQUISH, 1.0f, 0.5f);

                    // Broadcast message
                    if (ConfigManager.showPoopMessages()) {
                        String message = ConfigManager.getMessage("bodily.poop", "{player}", player.getName());
                        for (Player nearby : player.getWorld().getPlayers()) {
                            if (nearby.getLocation().distance(player.getLocation()) < 15) {
                                nearby.sendMessage(message);
                            }
                        }
                    }

                    // Easter egg: Check for nearby sheep/chickens to dye brown
                    AnimalInteractionHandler.checkForAnimals(player, "brown");

                    this.cancel();
                    return;
                }

                // Straining particles
                if (ticks % 5 == 0) {
                    player.getWorld().spawnParticle(Particle.SMOKE, player.getLocation().add(0, 1.5, 0), 3, 0.1, 0.1, 0.1, 0.01);
                }

                ticks++;
            }
        }.runTaskTimer(Plugin.instance, 0L, 1L);

        return true;
    }

    private boolean handlePiss(Player player) {
        // Check if genitals are out (penis for males/other, vagina for females)
        if (!GenderManager.hasActiveGenitals(player)) {
            player.sendMessage(ConfigManager.getMessage("bodily.piss-need-toggle"));
            return true;
        }

        if (CooldownManager.hasCooldown(player, "piss", PISS_COOLDOWN)) {
            player.sendMessage(ConfigManager.getMessage("bodily.cooldown"));
            return true;
        }

        CooldownManager.setCooldown(player, "piss");
        
        // Track statistic
        StatisticsManager.incrementPissCount(player);

        // Start piss stream
        new BukkitRunnable() {
            int ticks = 0;
            final int duration = 60; // 3 seconds

            @Override
            public void run() {
                if (ticks >= duration || !player.isOnline()) {
                    this.cancel();
                    return;
                }

                // Get player direction and position
                Location eyeLoc = player.getEyeLocation();
                Vector direction = eyeLoc.getDirection();
                
                // Start position (at crotch level, slightly in front)
                float heightOffset = player.isSneaking() ? 0.55f : 0.65f;
                Location startLoc = player.getLocation().clone().add(0, heightOffset, 0);
                startLoc.add(direction.clone().multiply(0.4));

                // Create sine wave piss stream
                double time = ticks * 0.15;
                
                // Yellow dust options for piss
                DustOptions yellowPiss = new DustOptions(Color.YELLOW, 0.5f);
                DustOptions darkYellowPiss = new DustOptions(Color.fromRGB(204, 204, 0), 0.7f);

                for (double t = 0; t < 2.5; t += 0.15) {
                    // Parabolic trajectory with sine wave
                    double x = t;
                    double sineOffset = Math.sin(time + t * 3) * 0.08; // Sine wave wobble
                    double y = -0.15 * t * t + 0.1 * t; // Parabola (gravity)
                    
                    // Calculate world position
                    Vector forward = direction.clone().normalize();
                    Vector right = new Vector(-forward.getZ(), 0, forward.getX()).normalize();
                    
                    Location particleLoc = startLoc.clone()
                        .add(forward.clone().multiply(x))
                        .add(right.clone().multiply(sineOffset))
                        .add(0, y, 0);

                    // Yellow dust particles for piss stream
                    player.getWorld().spawnParticle(Particle.DUST, particleLoc, 1, 0.02, 0.02, 0.02, 0, yellowPiss);
                    
                    // Occasional darker yellow drip for effect
                    if (Math.random() < 0.3) {
                        player.getWorld().spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0, 0, darkYellowPiss);
                    }
                }

                // Sound every few ticks
                if (ticks % 4 == 0) {
                    player.getWorld().playSound(startLoc, Sound.BLOCK_WATER_AMBIENT, 0.3f, 1.5f);
                }

                // Broadcast message at start
                if (ticks == 0 && ConfigManager.showPissMessages()) {
                    String message = ConfigManager.getMessage("bodily.piss", "{player}", player.getName());
                    for (Player nearby : player.getWorld().getPlayers()) {
                        if (nearby.getLocation().distance(player.getLocation()) < 15) {
                            nearby.sendMessage(message);
                        }
                    }
                }

                // Easter egg: Check for nearby sheep/chickens to dye yellow
                if (ticks % 10 == 0) {
                    AnimalInteractionHandler.checkForAnimals(player, "yellow");
                }

                ticks++;
            }
        }.runTaskTimer(Plugin.instance, 0L, 1L);

        return true;
    }

}
