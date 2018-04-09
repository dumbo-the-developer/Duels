package me.realized._duels.utilities.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public abstract class Config {
    private final String fileName;
    private final JavaPlugin instance;

    protected FileConfiguration base;

    public Config(String fileName, JavaPlugin instance) {
        this.fileName = fileName;
        this.instance = instance;
        reload(false);
    }

    public void reload(boolean handleLoad) {
        File file = new File(instance.getDataFolder(), fileName);

        if (!file.exists()) {
            instance.saveResource(fileName, false);
        }

        this.base = YamlConfiguration.loadConfiguration(file);

        if (handleLoad) {
            handleLoad();
        }
    }

    public abstract void handleLoad();
}
