package me.realized.duels.configuration;

import me.realized.duels.Core;
import me.realized.duels.utilities.config.Config;
import org.bukkit.configuration.MemorySection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessagesConfig extends Config {

    private final Map<String, String> strings = new HashMap<>();
    private final Map<String, List<String>> lists = new HashMap<>();

    public MessagesConfig(Core instance) {
        super("messages.yml", instance);
        handleLoad();
    }

    @Override
    public void handleLoad() {
        // Clearing in case of configuration reload
        strings.clear();
        lists.clear();

        for (String path : base.getKeys(true)) {
            Object value = base.get(path);

            if (value == null || value instanceof MemorySection) {
                continue;
            }

            if (value instanceof String && !((String) value).isEmpty()) {
                strings.put(path, (String) value);
            } else if (value instanceof List && !((List) value).isEmpty()){
                lists.put(path, base.getStringList(path));
            }
        }
    }

    public String getString(String path) {
        return strings.get(path);
    }

    public List<String> getList(String path) {
        return lists.get(path);
    }
}
