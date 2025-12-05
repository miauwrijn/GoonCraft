package com.miauwrijn.gooncraft.models;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import com.miauwrijn.gooncraft.data.PenisStatistics;
import com.miauwrijn.gooncraft.handlers.BodilyFunctionsHandler;
import com.miauwrijn.gooncraft.managers.ConfigManager;
import com.miauwrijn.gooncraft.managers.CooldownManager;
import com.miauwrijn.gooncraft.managers.RankManager;
import com.miauwrijn.gooncraft.managers.StatisticsManager;

public class PenisModel implements Runnable {

    public static final int minSize = 5;
    public static final int maxSize = 30;
    public static final int minGirth = 5;
    public static final int maxGirth = 15;

    private static final int CUM_ANIMATION_DURATION = 10;
    private static final int BASE_EJACULATE_CHANCE = 50; // 1 in 50 for rank 0

    private int size;
    private int girth;
    private int viagraBoost;
    private boolean bbc;
    private final Player owner;

    private BlockDisplay leftBall;
    private BlockDisplay rightBall;
    private BlockDisplay shaft;
    private BlockDisplay head;

    private boolean isCumming = false;
    private int framesSinceCumStart = 0;
    private float stretch = 0;

    public PenisModel(Player owner, boolean bbc, int size, int girth, int viagraBoost) {
        this.owner = owner;
        this.bbc = bbc;
        this.size = clamp(size, minSize, maxSize);
        this.girth = clamp(girth, minGirth, maxGirth);
        this.viagraBoost = viagraBoost;
        buildModel();
    }

    public static int getRandomSize() {
        return ThreadLocalRandom.current().nextInt(minSize, maxSize + 1);
    }

    public static int getRandomGirth() {
        return ThreadLocalRandom.current().nextInt(minGirth, maxGirth + 1);
    }

    public static boolean getRandomBbc() {
        return ThreadLocalRandom.current().nextBoolean();
    }

    public void reload(PenisStatistics stats) {
        this.size = clamp(stats.size, minSize, maxSize);
        this.girth = clamp(stats.girth, minGirth, maxGirth);
        this.bbc = stats.bbc;
        this.viagraBoost = stats.viagraBoost;
        discard();
        buildModel();
    }

    @Override
    public void run() {
        if (isCumming) {
            animateFap();
        }
        updatePosition();
    }

    public void discard() {
        if (leftBall != null) leftBall.remove();
        if (rightBall != null) rightBall.remove();
        if (shaft != null) shaft.remove();
        if (head != null) head.remove();
    }

    public void cum() {
        if (!owner.isSneaking() || isCumming) {
            return;
        }

        int cooldown = ThreadLocalRandom.current().nextInt(2);
        if (CooldownManager.hasCooldown(owner, "cum", cooldown)) {
            return;
        }

        isCumming = true;
        CooldownManager.setCooldown(owner, "cum");

        // Track fap statistic
        StatisticsManager.incrementFapCount(owner);

        Location location = owner.getLocation();
        World world = location.getWorld();
        List<Player> nearbyPlayers = world.getPlayers();
        
        // Higher rank = more frequent ejaculation
        // Rank 0 (Innocent Virgin) = 1 in 50 chance
        // Rank 11 (Ultimate Degenerate) = 1 in 5 chance
        RankManager.Rank rank = RankManager.getRank(owner);
        int rankBonus = rank.ordinal() * 4; // Each rank reduces the divisor by 4
        int ejaculateChance = Math.max(5, BASE_EJACULATE_CHANCE - rankBonus);
        int ejaculateRoll = ThreadLocalRandom.current().nextInt(ejaculateChance);
        boolean isEjaculating = ejaculateRoll == 0;

        for (Player player : nearbyPlayers) {
            double distance = player.getLocation().distance(location);
            if (distance < 10) {
                sendCumMessage(player, distance, isEjaculating);
            }
        }

        // Only spawn cum particles when ejaculating
        if (isEjaculating) {
            spawnCumParticles();
            // Easter egg: Check for nearby sheep/chickens to cover in white
            BodilyFunctionsHandler.checkForAnimals(owner, "white");
        }
    }

    private void animateFap() {
        float maxStretch = (size * 0.15f) * 0.03f;
        
        if (framesSinceCumStart >= CUM_ANIMATION_DURATION) {
            isCumming = false;
            stretch = 0;
            framesSinceCumStart = 0;
            return;
        }

        boolean extending = framesSinceCumStart <= CUM_ANIMATION_DURATION / 2;
        float delta = maxStretch / (CUM_ANIMATION_DURATION / 2f);
        stretch += extending ? delta : -delta;

        setBlockTransformation(shaft, true, false);
        setBlockTransformation(head, false, true);
        framesSinceCumStart++;
    }

    private void updatePosition() {
        Location location = owner.getLocation();
        location.setPitch(clamp(location.getPitch(), -10f, 10f));

        float heightOffset = owner.isSneaking() ? 0.55f : 0.65f;
        Location center = location.clone().add(0, heightOffset, 0);
        Vector direction = center.getDirection();

        if (owner.isSneaking()) {
            center = center.subtract(direction.clone().normalize().multiply(0.25));
            direction = center.getDirection();
        }

        Vector perpVector = new Vector(direction.getZ(), 0, -direction.getX());
        center = center.add(direction.clone().multiply(0.075));
        Location ballCenter = center.clone().subtract(new Vector(0, 0.075, 0));

        leftBall.teleport(ballCenter.clone().add(perpVector.clone().multiply(0.075)));
        rightBall.teleport(ballCenter.clone().subtract(perpVector.clone().multiply(0.075)));
        shaft.teleport(center.clone().add(direction.clone().multiply(0.075)));
        head.teleport(center.clone().add(direction.clone().multiply(0.09)));
    }

    private void buildModel() {
        Location location = owner.getLocation();
        World world = owner.getWorld();

        BlockData bodyBlock = (bbc ? Material.BROWN_TERRACOTTA : Material.WHITE_TERRACOTTA).createBlockData();
        BlockData headBlock = Material.TERRACOTTA.createBlockData();

        leftBall = createBlockDisplay(location, bodyBlock, world, false, false);
        rightBall = createBlockDisplay(location, bodyBlock, world, false, false);
        shaft = createBlockDisplay(location, bodyBlock, world, true, false);
        head = createBlockDisplay(location, headBlock, world, false, true);

        updatePosition();
    }

    private BlockDisplay createBlockDisplay(Location location, BlockData block, World world, 
                                            boolean isShaft, boolean isHead) {
        BlockDisplay display = (BlockDisplay) world.spawnEntity(location, EntityType.BLOCK_DISPLAY);
        display.setBlock(block);
        display.setDisplayHeight(0.1f);
        display.setDisplayWidth(0.1f);
        display.setGravity(false);
        display.setInvulnerable(true);
        display.setSilent(true);
        display.setCustomName("GOONCRAFT");
        display.setCustomNameVisible(false);

        setBlockTransformation(display, isShaft, isHead);
        return display;
    }

    private void setBlockTransformation(BlockDisplay block, boolean isShaft, boolean isHead) {
        float g = girth * 0.01f;
        int totalSize = size + viagraBoost;

        Vector3f scale;
        if (isShaft) {
            scale = new Vector3f(g, g, totalSize * 0.03f + stretch);
        } else if (isHead) {
            scale = new Vector3f(g * 0.75f, g * 0.75f, (totalSize + 1) * 0.03f + stretch);
        } else {
            scale = new Vector3f(g, g, g);
        }

        Vector3f translation = new Vector3f(-scale.x * 0.5f, -scale.y * 0.5f, 0f);
        AxisAngle4f rotation = new AxisAngle4f(0, 0, 1, 0);
        
        block.setTransformation(new Transformation(translation, rotation, scale, rotation));
    }

    private void sendCumMessage(Player player, double distance, boolean isEjaculating) {
        if (isEjaculating) {
            if (ConfigManager.showEjaculateMessages()) {
                player.sendMessage("<" + owner.getName() + "> " + ConfigManager.getMessage("cum.ejaculate"));
            }
        } else if (distance < 2 && player != owner) {
            if (ConfigManager.showCummedOnMessages()) {
                player.sendMessage(ConfigManager.getMessage("cum.cummed-on", "{player}", owner.getName()));
            }
            // Track statistic for getting cummed on
            StatisticsManager.incrementGotCummedOn(player);
            StatisticsManager.incrementCumOnOthers(owner);
        } else {
            if (ConfigManager.showFapMessages()) {
                player.sendMessage("<" + owner.getName() + "> " + ConfigManager.getMessage("cum.fap"));
            }
        }
    }

    private void spawnCumParticles() {
        Location location = owner.getLocation();
        location.setPitch(clamp(location.getPitch(), -10f, 10f));
        
        Location center = location.clone().add(0, 0.65, 0);
        Vector direction = center.getDirection();
        Location headLocation = head.getLocation().add(direction.multiply((size + 2) * 0.03f));

        World world = owner.getWorld();
        world.spawnParticle(Particle.CLOUD, headLocation, 20, 0.001, 0.001, 0.001, 0.1);
        world.playSound(owner.getLocation(), Sound.ENTITY_LLAMA_SPIT, 1.0f, 2.0f);
        world.playSound(owner.getLocation(), Sound.ENTITY_GHAST_SCREAM, 1.0f, 0.00001f);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
