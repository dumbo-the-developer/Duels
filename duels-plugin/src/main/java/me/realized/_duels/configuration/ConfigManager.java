package me.realized._duels.configuration;

import java.util.HashMap;
import java.util.Map;
import me.realized._duels.utilities.Reloadable;
import me.realized._duels.utilities.config.Config;

public class ConfigManager implements Reloadable {

    private static ConfigManager instance;

    private final Map<ConfigType, Config> configurations = new HashMap<>();

    public ConfigManager() {
        instance = this;
    }

    public static Config getConfig(ConfigType type) {
        return instance.getConfigByType(type);
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
}
