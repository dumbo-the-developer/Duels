package me.realized.duels.configuration;

import me.realized.duels.Core;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Config {

    private final Core instance;
    private final File base;

    private final Map<String, List<String>> lists = new HashMap<>();
    private final Map<String, String> strings = new HashMap<>();
    private final Map<String, Integer> numbers = new HashMap<>();
    private final Map<String, Boolean> booleans = new HashMap<>();

    public Config(Core instance) {
        this.instance = instance;
        this.base = new File(instance.getDataFolder(), "config.yml");
        load();
    }

    private void load() {
        if (!base.exists()) {
            instance.saveResource("config.yml", true);
            instance.info("Generated configuration file.");
        }

        if (instance.getWorldGuard() == null) {
            instance.warn("WorldGuard was not found! DuelZone will not work properly.");
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(base);

        if (!config.getKeys(false).isEmpty()) {
            for (String section : config.getKeys(false)) {
                if (config.isConfigurationSection(section)) {
                    for (String key : config.getConfigurationSection(section).getKeys(false)) {
                        String path = section + "." + key;

                        if (config.isList(path)) {
                            lists.put(key, config.getStringList(path));
                            continue;
                        }

                        if (config.isBoolean(path)) {
                            booleans.put(key, config.getBoolean(path));
                            continue;
                        }

                        if (config.isString(path)) {
                            strings.put(key, config.getString(path).replace("{NEWLINE}", "\n"));
                            continue;
                        }

                        if (config.isInt(path)) {
                            numbers.put(key, config.getInt(path));
                        }
                    }
                }
            }
        }
    }

    public String getString(String key) {
        return strings.get(key);
    }

    public List<String> getList(String key) {
        return lists.get(key);
    }

    public boolean getBoolean(String key) {
        return booleans.get(key);
    }

    public int getInt(String key) {
        return numbers.get(key);
    }
}
