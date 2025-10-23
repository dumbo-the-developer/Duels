package com.meteordevelopments.duels.config.converters;

import com.meteordevelopments.duels.util.config.convert.Converter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class AutoMergeConverter implements Converter {

    private final JavaPlugin plugin;
    private final String configName;
    private final String langName;

    public AutoMergeConverter(JavaPlugin plugin, String configName, String langName) {
        this.plugin = plugin;
        this.configName = configName;
        this.langName = langName;
    }

    @Override
    public Map<String, String> renamedKeys() {
        final Map<String, String> keys = new HashMap<>();
        // Add any key renames here if needed
        return keys;
    }

    /**
     * Automatically merges user config with default config, adding missing keys.
     * This method should be called during config loading to ensure all new keys are added.
     */
    public void autoMergeConfigs() {
        try {
            // Merge config.yml
            mergeConfigFile(configName);
            
            // Merge lang.yml
            mergeConfigFile(langName);
            
            plugin.getLogger().info("[AutoMerge] Successfully merged configuration files with defaults!");
        } catch (Exception e) {
            plugin.getLogger().warning("[AutoMerge] Failed to merge configuration files: " + e.getMessage());
        }
    }

    private void mergeConfigFile(String fileName) throws Exception {
        File userFile = new File(plugin.getDataFolder(), fileName);
        InputStream defaultStream = plugin.getResource(fileName);
        
        if (defaultStream == null) {
            plugin.getLogger().warning("[AutoMerge] Default " + fileName + " not found in resources!");
            return;
        }

        // Load user config
        FileConfiguration userConfig = YamlConfiguration.loadConfiguration(userFile);
        
        // Load default config
        FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new java.io.InputStreamReader(defaultStream));
        
        // Merge configs
        FileConfiguration mergedConfig = mergeConfigurations(userConfig, defaultConfig);
        
        // Save merged config
        mergedConfig.save(userFile);
        
        plugin.getLogger().info("[AutoMerge] Merged " + fileName + " with default values!");
    }

    /**
     * Merges user configuration with default configuration.
     * Preserves all user values while adding missing default keys.
     */
    public static FileConfiguration mergeConfigurations(FileConfiguration userConfig, FileConfiguration defaultConfig) {
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
