package com.miauwrijn.gooncraft;

import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;

/*
 * gooncraft java plugin
 */
public class Plugin extends JavaPlugin {
  private static final Logger LOGGER = Logger.getLogger("gooncraft");

  private KontvingerCommandHandler commandHandler;
  private PenisCommandhandler penisCommandHandler;
  private PillManager pillManager;

  public static Plugin instance;

  public void onEnable() {
    instance = this;
    LOGGER.info("gooncraft enabled");
    commandHandler = new KontvingerCommandHandler();
    penisCommandHandler = new PenisCommandhandler();
    pillManager = new PillManager();
    new PenisStatisticManager();
    getCommand("kontvinger").setExecutor(commandHandler);
    getCommand("penis").setExecutor(penisCommandHandler);
    getCommand("viagra").setExecutor(pillManager);

  }

  public void onDisable() {
    LOGGER.info("gooncraft disabled");
  }
}

