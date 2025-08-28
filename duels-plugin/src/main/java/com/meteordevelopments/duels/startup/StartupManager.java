package com.meteordevelopments.duels.startup;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.config.DatabaseConfig;
import com.meteordevelopments.duels.mongo.MongoService;
import com.meteordevelopments.duels.redis.RedisService;
import com.meteordevelopments.duels.util.CC;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages the startup process of the Duels plugin
 */
public class StartupManager {
    
    private static final String SPIGOT_INSTALLATION_URL = "https://www.spigotmc.org/wiki/spigot-installation/";
    private static final Logger LOGGER = Logger.getLogger("[Duels-Optimised]");
    
    private final DuelsPlugin plugin;
    
    public StartupManager(DuelsPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Handles the complete startup process
     * @return true if startup was successful, false otherwise
     */
    public boolean startup() {
        long start = System.currentTimeMillis();
        
        if (!initializeDatabase()) {
            return false;
        }
        
        if (!loadLogManager()) {
            return false;
        }
        
        if (!checkSpigotCompatibility()) {
            return false;
        }
        
        long end = System.currentTimeMillis();
        DuelsPlugin.sendMessage("&2Successfully completed startup in " + CC.getTimeDifferenceAndColor(start, end) + "&a.");
        
        return true;
    }
    
    /**
     * Initializes database connections (MongoDB and Redis)
     * @return true if successful, false otherwise
     */
    private boolean initializeDatabase() {
        // Load DB.yml
        try {
            DatabaseConfig databaseConfig = new DatabaseConfig(plugin);
            databaseConfig.handleLoad();
            plugin.setDatabaseConfig(databaseConfig);
        } catch (Exception ex) {
            DuelsPlugin.sendMessage("&cFailed to load DB.yml. Disabling plugin.");
            return false;
        }

        // Initialize MongoDB (required)
        MongoService mongoService = new MongoService(plugin);
        try {
            mongoService.connect();
            plugin.setMongoService(mongoService);
        } catch (Exception ex) {
            DuelsPlugin.sendMessage("&cFailed to connect to MongoDB. Disabling plugin.");
            return false;
        }
        
        // Initialize Redis (optional)
        RedisService redisService = new RedisService(plugin);
        try {
            redisService.connect();
            plugin.setRedisService(redisService);
        } catch (Exception ex) {
            DuelsPlugin.sendMessage("&eRedis connection failed, continuing without cross-server sync cache.");
            LOGGER.log(Level.WARNING, "Redis connection failed; continuing without Redis.", ex);
            plugin.setRedisService(null);
        }
        
        return true;
    }
    
    /**
     * Loads the log manager
     * @return true if successful, false otherwise
     */
    private boolean loadLogManager() {
        long start = System.currentTimeMillis();
        
        DuelsPlugin.sendMessage("&eLoading log manager...");
        try {
            plugin.initializeLogManager();
            DuelsPlugin.sendMessage("&dSuccessfully loaded Log Manager in &f[" + CC.getTimeDifferenceAndColor(start, System.currentTimeMillis()) + "&f]");
            return true;
        } catch (Exception ex) {
            DuelsPlugin.sendMessage("&c&lCould not load LogManager. Please contact the developer.");
            LOGGER.log(Level.SEVERE, "Could not load LogManager. Please contact the developer.", ex);
            return false;
        }
    }
    
    /**
     * Checks if the server is running on Spigot
     * @return true if compatible, false otherwise
     */
    private boolean checkSpigotCompatibility() {
        try {
            Class.forName("org.spigotmc.SpigotConfig");
            return true;
        } catch (ClassNotFoundException ex) {
            DuelsPlugin.sendMessage("&c&l================= *** DUELS LOAD FAILURE *** =================");
            DuelsPlugin.sendMessage("&c&lDuels requires a spigot server to run, but this server was not running on spigot!");
            DuelsPlugin.sendMessage("&c&lTo run your server on spigot, follow this guide: " + SPIGOT_INSTALLATION_URL);
            DuelsPlugin.sendMessage("&c&lSpigot is compatible with CraftBukkit/Bukkit plugins.");
            DuelsPlugin.sendMessage("&c&l================= *** DUELS LOAD FAILURE *** =================");
            return false;
        }
    }
    

}