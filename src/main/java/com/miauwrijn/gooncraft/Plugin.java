package com.miauwrijn.gooncraft;

import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

import com.miauwrijn.gooncraft.handlers.ButtFingerCommandHandler;
import com.miauwrijn.gooncraft.handlers.PenisCommandHandler;

/**
 * GoonCraft - The Minecraft plugin your server never knew it needed.
 */
public class Plugin extends JavaPlugin {
    
    private static final Logger LOGGER = Logger.getLogger("GoonCraft");
    public static Plugin instance;

    @Override
    public void onEnable() {
        instance = this;
        
        // Initialize managers
        new PenisStatisticManager();
        PillManager pillManager = new PillManager();
        
        // Register command handlers
        getCommand("buttfinger").setExecutor(new ButtFingerCommandHandler());
        getCommand("penis").setExecutor(new PenisCommandHandler());
        getCommand("viagra").setExecutor(pillManager);

        LOGGER.info("GoonCraft enabled! Time to get weird.");
    }

    @Override
    public void onDisable() {
        LOGGER.info("GoonCraft disabled. Put your pants back on.");
    }
}
