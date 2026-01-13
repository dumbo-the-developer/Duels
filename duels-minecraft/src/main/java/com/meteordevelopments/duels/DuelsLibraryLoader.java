package com.meteordevelopments.duels;

import net.byteflux.libby.BukkitLibraryManager;
import net.byteflux.libby.Library;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

/**
 * Handles runtime loading of dependencies using Libby.
 * This significantly reduces the plugin JAR size by downloading dependencies at runtime.
 * Libby supports both Paper and Spigot servers.
 */
public class DuelsLibraryLoader {
    
    private final BukkitLibraryManager libraryManager;
    private final Logger logger;

    public DuelsLibraryLoader(JavaPlugin plugin) {
        this.libraryManager = new BukkitLibraryManager(plugin);
        this.logger = plugin.getLogger();
        
        // Set download directory to plugins/Duels/libraries
        libraryManager.addMavenCentral();
        libraryManager.addRepository("https://repo.papermc.io/repository/maven-public/");
        libraryManager.addRepository("https://mvn-repo.arim.space/lesser-gpl3/");
        libraryManager.addRepository("https://libraries.minecraft.net/");
    }

    /**
     * Loads all required dependencies at runtime.
     * This method should be called before any other plugin initialization.
     */
    public void loadDependencies() {
        logger.info("Loading runtime dependencies...");
        
        long startTime = System.currentTimeMillis();
        int loaded = 0;
        int failed = 0;
        
        // Load Jackson dependencies in correct order (annotations and core before databind)
        if (loadLibrary("com{}fasterxml{}jackson{}core", "jackson-annotations", "2.20.1")) loaded++; else failed++;
        if (loadLibrary("com{}fasterxml{}jackson{}core", "jackson-core", "2.20.1")) loaded++; else failed++;
        if (loadLibrary("com{}fasterxml{}jackson{}core", "jackson-databind", "2.20.1")) loaded++; else failed++;
        
        // JSON library
        if (loadLibrary("org{}json", "json", "20251224")) loaded++; else failed++;
        
        // Apache Commons Lang
        if (loadLibrary("commons-lang", "commons-lang", "2.6")) loaded++; else failed++;
        
        // Google Guava
        if (loadLibrary("com{}google{}guava", "guava", "33.2.1-jre")) loaded++; else failed++;
        
        // PaperLib for cross-version compatibility
        if (loadLibrary("io{}papermc", "paperlib", "1.0.8")) loaded++; else failed++;
        
        // Mojang AuthLib
        if (loadLibrary("com{}mojang", "authlib", "1.5.25")) loaded++; else failed++;
        
        // Adventure API for modern text components (load in dependency order)
        if (loadLibrary("net{}kyori", "examination-api", "1.3.0")) loaded++; else failed++;
        if (loadLibrary("net{}kyori", "examination-string", "1.3.0")) loaded++; else failed++;
        if (loadLibrary("net{}kyori", "adventure-key", "4.26.1")) loaded++; else failed++;
        if (loadLibrary("net{}kyori", "adventure-api", "4.26.1")) loaded++; else failed++;
        if (loadLibrary("net{}kyori", "adventure-nbt", "4.26.1")) loaded++; else failed++;
        if (loadLibrary("net{}kyori", "adventure-text-minimessage", "4.26.1")) loaded++; else failed++;
        if (loadLibrary("net{}kyori", "adventure-text-serializer-legacy", "4.26.1")) loaded++; else failed++;
        
        long endTime = System.currentTimeMillis();
        logger.info("Loaded " + loaded + " runtime dependencies in " + (endTime - startTime) + "ms" + 
                    (failed > 0 ? " (" + failed + " failed)" : ""));
    }

    /**
     * Loads a single library from Maven repositories.
     * Using {} instead of . to avoid issues with maven-shade-plugin.
     * 
     * @return true if loaded successfully, false otherwise
     */
    private boolean loadLibrary(String groupId, String artifactId, String version) {
        Library library = Library.builder()
                .groupId(groupId)
                .artifactId(artifactId)
                .version(version)
                .build();
        
        try {
            libraryManager.loadLibrary(library);
            logger.fine("Loaded library: " + groupId.replace("{}", ".") + ":" + artifactId + ":" + version);
            return true;
        } catch (Exception e) {
            logger.warning("Failed to load library: " + groupId.replace("{}", ".") + ":" + artifactId + ":" + version);
            logger.warning("Reason: " + e.getMessage());
            return false;
        }
    }
}
