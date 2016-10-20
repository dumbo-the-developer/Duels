package me.realized.duels.configuration;

import me.realized.duels.utilities.ICanHandleReload;
import me.realized.duels.utilities.ReloadType;
import me.realized.duels.utilities.config.Config;

import java.util.HashMap;
import java.util.Map;

public class ConfigManager implements ICanHandleReload {

    private static ConfigManager instance;

    private final Map<ConfigType, Config> configurations = new HashMap<>();

    public ConfigManager() {
        instance = this;
    }

    public void register(ConfigType type, Config config) {
        configurations.put(type, config);
    }

    public Config getConfigByType(ConfigType type) {
        return configurations.get(type);
    }

    @Override
    public void handleReload(ReloadType type) {
        switch (type) {
            case WEAK:
                getConfigByType(ConfigType.MESSAGES).reload(true);
                break;
            case STRONG:
                for (Config config : configurations.values()) {
                    config.reload(true);
                }
        }
    }

    public static Config getConfig(ConfigType type) {
        return instance.getConfigByType(type);
    }
}
