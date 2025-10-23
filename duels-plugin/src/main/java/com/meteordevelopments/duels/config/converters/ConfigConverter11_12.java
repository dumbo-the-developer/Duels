package com.meteordevelopments.duels.config.converters;

import com.meteordevelopments.duels.util.config.convert.Converter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ConfigConverter11_12 implements Converter {

    @Override
    public Map<String, String> renamedKeys() {
        final Map<String, String> keys = new HashMap<>();
        // Add any key renames here if needed
        return keys;
    }

    /**
     * Merges the default configuration with the existing user configuration.
     * This ensures all new keys are added while preserving user values.
     */
    public static FileConfiguration mergeWithDefaults(FileConfiguration userConfig, InputStream defaultConfigStream) {
        // Load the default configuration
        FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new java.io.InputStreamReader(defaultConfigStream));
        
        // Create a new configuration that starts with defaults
        FileConfiguration mergedConfig = new YamlConfiguration();
        
        // Copy all default values first
        for (String key : defaultConfig.getKeys(true)) {
            if (!defaultConfig.isConfigurationSection(key)) {
                mergedConfig.set(key, defaultConfig.get(key));
            }
        }
        
        // Override with user values (preserving user customizations)
        for (String key : userConfig.getKeys(true)) {
            if (!userConfig.isConfigurationSection(key)) {
                mergedConfig.set(key, userConfig.get(key));
            }
        }
        
        return mergedConfig;
    }

    /**
     * Automatically adds missing keys from default config to user config.
     * Preserves all existing user values while adding new keys.
     */
    public static FileConfiguration addMissingKeys(FileConfiguration userConfig, FileConfiguration defaultConfig) {
        FileConfiguration result = new YamlConfiguration();
        
        // First, copy all default values
        for (String key : defaultConfig.getKeys(true)) {
            if (!defaultConfig.isConfigurationSection(key)) {
                result.set(key, defaultConfig.get(key));
            }
        }
        
        // Then override with user values (preserving user customizations)
        for (String key : userConfig.getKeys(true)) {
            if (!userConfig.isConfigurationSection(key)) {
                result.set(key, userConfig.get(key));
            }
        }
        
        return result;
    }
}
