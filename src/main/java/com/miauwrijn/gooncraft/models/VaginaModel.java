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

import com.miauwrijn.gooncraft.handlers.BodilyFunctionsHandler;
import com.miauwrijn.gooncraft.managers.ConfigManager;
import com.miauwrijn.gooncraft.managers.CooldownManager;
import com.miauwrijn.gooncraft.managers.RankManager;
import com.miauwrijn.gooncraft.managers.StatisticsManager;

/**
 * 3D vagina model using BlockDisplay entities.
 * Can "goon" (masturbate) similar to PenisModel.
 */
public class VaginaModel implements Runnable {

    private static final int GOON_ANIMATION_DURATION = 15;
    private static final int BASE_ORGASM_CHANCE = 50; // 1 in 50 for rank 0

    private final Player owner;

    private BlockDisplay outerLeft;
    private BlockDisplay outerRight;
    private BlockDisplay inner;
    private BlockDisplay clit;

    private boolean isGooning = false;
    private int framesSinceGoonStart = 0;
    private float pulse = 0;

    public VaginaModel(Player owner) {
        this.owner = owner;
        buildModel();
    }

    @Override
    public void run() {
        if (isGooning) {
            animateGoon();
        }
        updatePosition();
    }

    public void discard() {
        if (outerLeft != null) outerLeft.remove();
        if (outerRight != null) outerRight.remove();
        if (inner != null) inner.remove();
        if (clit != null) clit.remove();
    }

    /**
     * Trigger gooning animation (equivalent to penis fapping).
     */
    public void goon() {
        if (!owner.isSneaking() || isGooning) {
            return;
        }

        int cooldown = ThreadLocalRandom.current().nextInt(2);
        if (CooldownManager.hasCooldown(owner, "goon", cooldown)) {
            return;
        }

        isGooning = true;
        CooldownManager.setCooldown(owner, "goon");

        // Track goon statistic
        StatisticsManager.incrementGoonCount(owner);

        Location location = owner.getLocation();
        World world = location.getWorld();
        List<Player> nearbyPlayers = world.getPlayers();

        // Higher rank = more frequent orgasm
        RankManager.Rank rank = RankManager.getRank(owner);
        int rankBonus = rank.ordinal() * 4;
        int orgasmChance = Math.max(5, BASE_ORGASM_CHANCE - rankBonus);
        int orgasmRoll = ThreadLocalRandom.current().nextInt(orgasmChance);
        boolean isOrgasming = orgasmRoll == 0;

        for (Player player : nearbyPlayers) {
            double distance = player.getLocation().distance(location);
            if (distance < 10) {
                sendGoonMessage(player, distance, isOrgasming);
            }
        }

        // Spawn particles when orgasming
        if (isOrgasming) {
            spawnOrgasmParticles();
            // Easter egg: Check for nearby animals
            BodilyFunctionsHandler.checkForAnimals(owner, "white");
        }
    }

    private void animateGoon() {
        if (framesSinceGoonStart >= GOON_ANIMATION_DURATION) {
            isGooning = false;
            pulse = 0;
            framesSinceGoonStart = 0;
            updateTransformations();
            return;
        }

        // Pulsing effect
        float progress = (float) framesSinceGoonStart / GOON_ANIMATION_DURATION;
        pulse = (float) Math.sin(progress * Math.PI * 4) * 0.01f;

        updateTransformations();
        framesSinceGoonStart++;
    }

    private void updatePosition() {
        Location location = owner.getLocation();
        
        // Only use yaw (horizontal rotation), not pitch
        float yaw = location.getYaw();
        
        float heightOffset = owner.isSneaking() ? 0.55f : 0.65f;
        Location center = location.clone().add(0, heightOffset, 0);
        
        // Calculate direction from yaw only
        double yawRad = Math.toRadians(yaw);
        Vector direction = new Vector(-Math.sin(yawRad), 0, Math.cos(yawRad)).normalize();
        Vector perpVector = new Vector(-direction.getZ(), 0, direction.getX()).normalize();

        // Position in front of crotch
        Location crotchCenter = center.clone().add(direction.clone().multiply(0.12));
        crotchCenter.setYaw(yaw);

        float spacing = 0.025f;
        
        Location leftPos = crotchCenter.clone().add(perpVector.clone().multiply(spacing));
        Location rightPos = crotchCenter.clone().subtract(perpVector.clone().multiply(spacing));
        Location innerPos = crotchCenter.clone();
        Location clitPos = crotchCenter.clone().add(0, 0.02, 0).add(direction.clone().multiply(0.01));
        
        leftPos.setYaw(yaw);
        rightPos.setYaw(yaw);
        innerPos.setYaw(yaw);
        clitPos.setYaw(yaw);

        outerLeft.teleport(leftPos);
        outerRight.teleport(rightPos);
        inner.teleport(innerPos);
        clit.teleport(clitPos);
    }

    private void buildModel() {
        Location location = owner.getLocation();
        World world = owner.getWorld();

        BlockData outerBlock = Material.PINK_TERRACOTTA.createBlockData();
        BlockData innerBlock = Material.RED_TERRACOTTA.createBlockData();
        BlockData clitBlock = Material.MAGENTA_TERRACOTTA.createBlockData();

        outerLeft = createBlockDisplay(location, outerBlock, world);
        outerRight = createBlockDisplay(location, outerBlock, world);
        inner = createBlockDisplay(location, innerBlock, world);
        clit = createBlockDisplay(location, clitBlock, world);

        updateTransformations();
        updatePosition();
    }

    private BlockDisplay createBlockDisplay(Location location, BlockData block, World world) {
        BlockDisplay display = (BlockDisplay) world.spawnEntity(location, EntityType.BLOCK_DISPLAY);
        display.setBlock(block);
        display.setDisplayHeight(0.1f);
        display.setDisplayWidth(0.1f);
        display.setGravity(false);
        display.setInvulnerable(true);
        display.setSilent(true);
        display.setCustomName("GOONCRAFT");
        display.setCustomNameVisible(false);

        return display;
    }

    private void updateTransformations() {
        // Outer lips
        float outerWidth = 0.02f + pulse;
        float outerHeight = 0.06f;
        float outerDepth = 0.015f;

        Vector3f outerScale = new Vector3f(outerWidth, outerHeight, outerDepth);
        Vector3f outerTranslation = new Vector3f(-outerScale.x * 0.5f, -outerScale.y * 0.5f, 0f);
        AxisAngle4f rotation = new AxisAngle4f(0, 0, 1, 0);

        Transformation outerTransform = new Transformation(outerTranslation, rotation, outerScale, rotation);
        
        if (outerLeft != null) outerLeft.setTransformation(outerTransform);
        if (outerRight != null) outerRight.setTransformation(outerTransform);

        // Inner part
        float innerWidth = 0.015f + pulse * 0.5f;
        float innerHeight = 0.04f;
        float innerDepth = 0.02f;

        Vector3f innerScale = new Vector3f(innerWidth, innerHeight, innerDepth);
        Vector3f innerTranslation = new Vector3f(-innerScale.x * 0.5f, -innerScale.y * 0.5f, 0f);

        Transformation innerTransform = new Transformation(innerTranslation, rotation, innerScale, rotation);
        if (inner != null) inner.setTransformation(innerTransform);

        // Clit
        float clitSize = 0.008f + pulse * 0.3f;

        Vector3f clitScale = new Vector3f(clitSize, clitSize, clitSize);
        Vector3f clitTranslation = new Vector3f(-clitScale.x * 0.5f, -clitScale.y * 0.5f, 0f);

        Transformation clitTransform = new Transformation(clitTranslation, rotation, clitScale, rotation);
        if (clit != null) clit.setTransformation(clitTransform);
    }

    private void sendGoonMessage(Player player, double distance, boolean isOrgasming) {
        if (isOrgasming) {
            if (ConfigManager.showEjaculateMessages()) {
                player.sendMessage("<" + owner.getName() + "> " + ConfigManager.getMessage("goon.orgasm"));
            }
        } else if (distance < 2 && player != owner) {
            if (ConfigManager.showCummedOnMessages()) {
                player.sendMessage(ConfigManager.getMessage("goon.squirted-on", "{player}", owner.getName()));
            }
            // Track statistic
            StatisticsManager.incrementGotSquirtedOn(player, owner);
            StatisticsManager.incrementSquirtOnOthers(owner, player);
        } else {
            if (ConfigManager.showFapMessages()) {
                player.sendMessage("<" + owner.getName() + "> " + ConfigManager.getMessage("goon.goon"));
            }
        }
    }

    private void spawnOrgasmParticles() {
        Location location = owner.getLocation();
        float heightOffset = owner.isSneaking() ? 0.55f : 0.65f;
        Location center = location.clone().add(0, heightOffset, 0);
        
        double yawRad = Math.toRadians(location.getYaw());
        Vector direction = new Vector(-Math.sin(yawRad), 0, Math.cos(yawRad)).normalize();
        Location orgasmLoc = center.clone().add(direction.clone().multiply(0.15));

        World world = owner.getWorld();
        world.spawnParticle(Particle.CLOUD, orgasmLoc, 15, 0.05, 0.05, 0.05, 0.05);
        world.spawnParticle(Particle.DRIPPING_WATER, orgasmLoc, 10, 0.1, 0.1, 0.1, 0);
        world.playSound(owner.getLocation(), Sound.ENTITY_WITCH_CELEBRATE, 0.8f, 1.5f);
    }
}
