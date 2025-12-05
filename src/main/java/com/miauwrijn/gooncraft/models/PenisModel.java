package com.miauwrijn.gooncraft.models;

import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import com.miauwrijn.gooncraft.CooldownManager;
import com.miauwrijn.gooncraft.data.PenisStatistics;

import net.md_5.bungee.api.ChatColor;

public class PenisModel implements Runnable {

    public static int minSize = 5;
    public static int maxSize = 30;

    public static int getRandomSize() {
        Random random = new Random();
        return random.nextInt(maxSize - minSize) + minSize;
    }

    public static int minGirth = 5;
    public static int maxGirth = 15;

    public static int getRandomGirth() {
        Random random = new Random();
        return random.nextInt(maxGirth - minGirth) + minGirth;
    }

    public static boolean getRandomBbc() {
        Random random = new Random();
        return random.nextBoolean();
    }

    private int size;
    private int girth;

    private boolean bbc;
    private Player owner;

    public PenisModel(Player receiver, boolean bbc, int size, int girth, int viagraBoost) {
        this.owner = receiver;
        this.bbc = bbc;
        this.size = size;
        this.girth = girth;
        this.viagraBoost = viagraBoost;

        buildDick();
    }

    int viagraBoost = 0;

    public void reload(PenisStatistics updatedStatistics) {
        this.size = updatedStatistics.size;
        this.girth = updatedStatistics.girth;
        this.bbc = updatedStatistics.bbc;
        this.viagraBoost = updatedStatistics.viagraBoost;
        discard();
        buildDick();
    }

    boolean isCumming = false;
    int framesSineCumStart = 0;
    private final int cumAnimationDuration = 10;

    @Override
    public void run() {
        // move dick
        if (isCumming) {
            animateFap();
        }
        moveDick();
    }

    float _stretch = 0;

    void animateFap() {
        float maximumStretch = ((float) size * 0.15f) * 0.03f;
        if (framesSineCumStart == cumAnimationDuration) {
            isCumming = false;
            _stretch = 0;
            framesSineCumStart = 0;
            return;
        } else {
            // set fap animationFrame
            boolean fapOut = framesSineCumStart <= ((float) cumAnimationDuration / 2f);
            if (fapOut) {
                // animate out
                _stretch += (float) maximumStretch / ((float) cumAnimationDuration / 2f);
            } else {
                // animate back
                _stretch -= (float) maximumStretch / ((float) cumAnimationDuration / 2f);
            }
        }
        setBlockTransformation(shaft, true, false);
        setBlockTransformation(head, false, true);
        framesSineCumStart++;
    }

    public void discard() {

        leftBall.remove();
        rightBall.remove();
        shaft.remove();
        head.remove();

    }

    void moveDick() {
        Location location = owner.getLocation();

        // get direction of player body
        if (location.getPitch() > 10f) {
            location.setPitch(10f);
        } else if (location.getPitch() < -10f) {
            location.setPitch(-10f);
        }

        // get center of player
        Location center = location.clone().add(0, owner.isSneaking() ? 0.55 : 0.65, 0);
        Vector direction = center.getDirection();
        if (owner.isSneaking()) {
            center = center.clone().subtract(direction.clone().normalize().multiply(0.25));
            direction = center.getDirection();
        }
        // set the yaw
        Vector perpVector = new Vector(direction.getZ(), 0, -direction.getX());
        center = center.add(direction.clone().multiply(0.075));
        Location ballCenter = center.clone().subtract(new Vector(0, 0.075, 0));
        // get the 2 points to build

        Location left = ballCenter.clone().add(perpVector.clone().multiply(0.075));
        Location right = ballCenter.clone().subtract(perpVector.clone().multiply(0.075));
        leftBall.teleport(left);
        rightBall.teleport(right);

        float offset = 0.075f;
        shaft.teleport(center.clone().add(direction.clone().multiply(offset)));
        head.teleport(center.clone().add(direction.clone().multiply(offset + 0.015f)));
    }

    BlockDisplay leftBall = null;
    BlockDisplay rightBall = null;

    BlockDisplay shaft = null;
    BlockDisplay head = null;

    void buildDick() {

        Location location = owner.getLocation();
        World world = owner.getWorld();

        BlockData block = bbc ? org.bukkit.Material.BROWN_TERRACOTTA.createBlockData()
                : org.bukkit.Material.WHITE_TERRACOTTA.createBlockData();

        BlockData headBlock = org.bukkit.Material.TERRACOTTA.createBlockData();

        leftBall = buildBlock(location, block, world, false, false);
        rightBall = buildBlock(location, block, world, false, false);

        shaft = buildBlock(location, block, world, true, false);

        head = buildBlock(location, headBlock, world, false, true);

        moveDick();
    }

    BlockDisplay buildBlock(Location location, BlockData block, World world, boolean shaftBlock, boolean headBlock) {

        // make sure size is between 1 and 30
        if (size < minSize) {
            size = minSize;
        } else if (size > maxSize) {
            size = maxSize;
        }

        // make sure girth is between 1 and 15
        if (girth < minGirth) {
            girth = minGirth;
        } else if (girth > maxGirth) {
            girth = maxGirth;
        }

        // block display entity
        BlockDisplay blockDisplay = (BlockDisplay) world.spawnEntity(location, EntityType.BLOCK_DISPLAY);
        blockDisplay.setBlock(block);
        // set size of block
        blockDisplay.setDisplayHeight(0.1f);
        blockDisplay.setDisplayWidth(0.1f);

        setBlockTransformation(blockDisplay, shaftBlock, headBlock);

        blockDisplay.setGravity(false);
        blockDisplay.setInvulnerable(true);
        blockDisplay.setSilent(true);
        blockDisplay.setCustomName("GOONCRAFT");
        blockDisplay.setCustomNameVisible(false);

        return blockDisplay;
    }

    void setBlockTransformation(BlockDisplay block, boolean shaftBlock,
            boolean headBlock) {

        // modify entity data to set the size transformation
        Vector3f scale = new Vector3f(0.01f * girth, 0.01f * girth, 0.01f * girth);
        if (shaftBlock) {
            scale = new Vector3f(0.01f * girth, 0.01f * girth, (0.03f * (size + viagraBoost) + _stretch));
        }
        if (headBlock) {
            scale = new Vector3f(0.0075f * girth, 0.0075f * girth, (0.03f * (size + 1 + viagraBoost)) + _stretch);
        }
        Vector3f translation = new Vector3f(-(0.5f * scale.x), -(0.5f * scale.y), 0f);

        AxisAngle4f leftRotation = new AxisAngle4f(0, 0, 1, 0);
        AxisAngle4f rightRotation = new AxisAngle4f(0, 0, 1, 0);

        Transformation transformation = new Transformation(translation, leftRotation, scale, rightRotation);
        block.setTransformation(transformation);
    }

    public void cum() {
        if (!owner.isSneaking()) {
            return;
        }

        Random random = new Random();
        int cooldown = random.nextInt(2);
        if (CooldownManager.hasCooldown(owner, "cum", cooldown) || isCumming) {
            return;
        }

        isCumming = true;
        // start cooldown
        CooldownManager.setCooldown(owner, "cum");

        // start a runnable task that spawns a trail of white particles from the head

        Location location = owner.getLocation();

        World world = location.getWorld();
        List<Player> players = world.getPlayers();

        int randomEjaculateChance = random.nextInt(50);

        for (Player player : players) {
            double distance = player.getLocation().distance(location);

            if (distance < 10) {
                if (randomEjaculateChance == 1) {
                    player.sendMessage("<" + owner.getName() + "> " + ChatColor.GRAY + "Hmmfff.. *ejaculates*");
                } else {
                    if (distance < 2 && player != owner) {
                        player.sendMessage("" +
                                ChatColor.GOLD + "You have been cummed on by " + ChatColor.GREEN
                                + ChatColor.BOLD + owner.getName());
                    } else {
                        player.sendMessage("<" + owner.getName() + "> " + ChatColor.GRAY + "Fap...");
                    }
                }
            }
        }

        // get direction of player body
        if (location.getPitch() > 10f) {
            location.setPitch(10f);
        } else if (location.getPitch() < -10f) {
            location.setPitch(-10f);
        }

        // get center of player
        Location center = location.clone().add(0, 0.65, 0);
        Vector direction = center.getDirection();

        Location headLocation = head.getLocation().clone().add(direction.clone().multiply(0.03f * (size + 2)));

        owner.getWorld().spawnParticle(org.bukkit.Particle.CLOUD,
                headLocation, 1, 0.001, 0.001, 0.001,
                0.1);

        // play sound
        owner.getWorld().playSound(owner.getLocation(), org.bukkit.Sound.ENTITY_LLAMA_SPIT, 10, 2);

        if (randomEjaculateChance == 1) {

            owner.getWorld().spawnParticle(org.bukkit.Particle.CLOUD,
                    headLocation, 20, 0.001, 0.001, 0.001,
                    0.1);
            owner.getWorld().playSound(owner.getLocation(), org.bukkit.Sound.ENTITY_GHAST_SCREAM, 10, 0.00001f);

        }
    }
}

