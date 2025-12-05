package com.miauwrijn.gooncraft.handlers;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Wolf;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.miauwrijn.gooncraft.Plugin;
import com.miauwrijn.gooncraft.managers.AchievementManager;
import com.miauwrijn.gooncraft.managers.AchievementManager.Achievement;
import com.miauwrijn.gooncraft.managers.ConfigManager;
import com.miauwrijn.gooncraft.managers.CooldownManager;
import com.miauwrijn.gooncraft.managers.GenderManager;
import com.miauwrijn.gooncraft.managers.StatisticsManager;

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
        player.getWorld().spawnParticle(Particle.ENTITY_EFFECT, particleLoc, 10, 0.3, 0.1, 0.3, 1);

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
                    checkForAnimals(player, "brown");

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
                    checkForAnimals(player, "yellow");
                }

                ticks++;
            }
        }.runTaskTimer(Plugin.instance, 0L, 1L);

        return true;
    }

    /**
     * Easter egg: Check for nearby animals and affect them!
     * @param player The player
     * @param color "yellow" for piss, "brown" for poop, "white" for cum
     */
    public static void checkForAnimals(Player player, String color) {
        Location loc = player.getLocation();
        
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, 3, 3, 3)) {
            // === SHEEP ===
            if (entity instanceof Sheep sheep) {
                DyeColor dyeColor = switch (color) {
                    case "yellow" -> DyeColor.YELLOW;
                    case "brown" -> DyeColor.BROWN;
                    case "white" -> DyeColor.WHITE;
                    default -> null;
                };
                
                if (dyeColor != null && sheep.getColor() != dyeColor) {
                    sheep.setColor(dyeColor);
                    
                    // Spawn particles on the sheep
                    DustOptions dust = new DustOptions(
                        color.equals("yellow") ? Color.YELLOW : 
                        color.equals("brown") ? Color.fromRGB(139, 69, 19) : Color.WHITE, 
                        1.0f
                    );
                    sheep.getWorld().spawnParticle(Particle.DUST, sheep.getLocation().add(0, 1, 0), 
                        20, 0.5, 0.5, 0.5, 0, dust);
                    sheep.getWorld().playSound(sheep.getLocation(), Sound.ENTITY_SHEEP_AMBIENT, 1.0f, 1.5f);
                    
                    // Unlock achievement
                    Achievement achievement = switch (color) {
                        case "yellow" -> Achievement.YELLOW_SHEEP;
                        case "brown" -> Achievement.BROWN_SHEEP;
                        case "white" -> Achievement.WHITE_SHEEP;
                        default -> null;
                    };
                    if (achievement != null) {
                        AchievementManager.tryUnlock(player, achievement);
                    }
                }
            }
            // === CHICKEN ===
            else if (entity instanceof Chicken chicken) {
                // For chickens, we can't dye them, but we can add effects!
                DustOptions dust = new DustOptions(
                    color.equals("yellow") ? Color.YELLOW : 
                    color.equals("brown") ? Color.fromRGB(139, 69, 19) : Color.WHITE, 
                    0.8f
                );
                
                // Shower the chicken with colored particles
                chicken.getWorld().spawnParticle(Particle.DUST, chicken.getLocation().add(0, 0.5, 0), 
                    15, 0.3, 0.3, 0.3, 0, dust);
                chicken.getWorld().playSound(chicken.getLocation(), Sound.ENTITY_CHICKEN_HURT, 1.0f, 1.2f);
                
                // Give the chicken a funny custom name
                String chickenName = switch (color) {
                    case "yellow" -> "§e§lLemon Chicken";
                    case "brown" -> "§6§lChocolate Nugget";
                    case "white" -> "§f§lCreamed Chicken";
                    default -> null;
                };
                if (chickenName != null && !chickenName.equals(chicken.getCustomName())) {
                    chicken.setCustomName(chickenName);
                    chicken.setCustomNameVisible(true);
                    
                    // Unlock achievement
                    Achievement achievement = switch (color) {
                        case "yellow" -> Achievement.YELLOW_CHICKEN;
                        case "brown" -> Achievement.BROWN_CHICKEN;
                        case "white" -> Achievement.WHITE_CHICKEN;
                        default -> null;
                    };
                    if (achievement != null) {
                        AchievementManager.tryUnlock(player, achievement);
                    }
                }
            }
            // === PIG (white only) ===
            else if (entity instanceof Pig pig && color.equals("white")) {
                DustOptions dust = new DustOptions(Color.WHITE, 1.0f);
                pig.getWorld().spawnParticle(Particle.DUST, pig.getLocation().add(0, 0.5, 0), 
                    20, 0.4, 0.4, 0.4, 0, dust);
                pig.getWorld().playSound(pig.getLocation(), Sound.ENTITY_PIG_AMBIENT, 1.0f, 1.5f);
                
                if (pig.getCustomName() == null || !pig.getCustomName().contains("Glazed")) {
                    pig.setCustomName("§f§lGlazed Ham");
                    pig.setCustomNameVisible(true);
                    AchievementManager.tryUnlock(player, Achievement.WHITE_PIG);
                    StatisticsManager.incrementPigsAffected(player);
                }
            }
            // === COW (white only) ===
            else if (entity instanceof Cow cow && color.equals("white")) {
                DustOptions dust = new DustOptions(Color.WHITE, 1.0f);
                cow.getWorld().spawnParticle(Particle.DUST, cow.getLocation().add(0, 1, 0), 
                    25, 0.5, 0.5, 0.5, 0, dust);
                cow.getWorld().playSound(cow.getLocation(), Sound.ENTITY_COW_AMBIENT, 1.0f, 1.5f);
                
                if (cow.getCustomName() == null || !cow.getCustomName().contains("Cream")) {
                    cow.setCustomName("§f§lCream Cow");
                    cow.setCustomNameVisible(true);
                    AchievementManager.tryUnlock(player, Achievement.WHITE_COW);
                    StatisticsManager.incrementCowsAffected(player);
                }
            }
            // === WOLF (yellow only) ===
            else if (entity instanceof Wolf wolf && color.equals("yellow")) {
                DustOptions dust = new DustOptions(Color.YELLOW, 0.8f);
                wolf.getWorld().spawnParticle(Particle.DUST, wolf.getLocation().add(0, 0.7, 0), 
                    20, 0.4, 0.4, 0.4, 0, dust);
                wolf.getWorld().playSound(wolf.getLocation(), Sound.ENTITY_WOLF_WHINE, 1.0f, 1.2f);
                
                if (wolf.getCustomName() == null || !wolf.getCustomName().contains("Golden")) {
                    wolf.setCustomName("§e§lGolden Retriever");
                    wolf.setCustomNameVisible(true);
                    AchievementManager.tryUnlock(player, Achievement.YELLOW_WOLF);
                    StatisticsManager.incrementWolvesAffected(player);
                }
            }
            // === CAT (white only) ===
            else if (entity instanceof Cat cat && color.equals("white")) {
                DustOptions dust = new DustOptions(Color.WHITE, 0.6f);
                cat.getWorld().spawnParticle(Particle.DUST, cat.getLocation().add(0, 0.4, 0), 
                    15, 0.3, 0.3, 0.3, 0, dust);
                cat.getWorld().playSound(cat.getLocation(), Sound.ENTITY_CAT_HISS, 1.0f, 1.5f);
                
                if (cat.getCustomName() == null || !cat.getCustomName().contains("Cream")) {
                    cat.setCustomName("§f§lCream Puff");
                    cat.setCustomNameVisible(true);
                    AchievementManager.tryUnlock(player, Achievement.WHITE_CAT);
                    StatisticsManager.incrementCatsAffected(player);
                }
            }
        }
    }
}
