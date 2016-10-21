package me.realized.duels.utilities;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Storage {

    private static Map<UUID, Storage> storage = new HashMap<>();

    public static Storage get(Player player) {
        Storage instance = storage.get(player.getUniqueId());

        if (instance == null) {
            storage.put(player.getUniqueId(), new Storage(player.getUniqueId()));
            instance = storage.get(player.getUniqueId());
        }

        return instance;
    }

    public static void remove(Player player) {
        storage.remove(player.getUniqueId());
    }

    private final UUID owner;
    private final Map<String, Object> data = new HashMap<>();

    public Storage(UUID owner) {
        this.owner = owner;
    }

    public UUID getOwner() {
        return owner;
    }

    public void set(String key, Object value) {
        data.put(key, value);
    }

    public void remove(String key) {
        data.remove(key);
    }

    public Object get(String key) {
        return data.get(key);
    }
}
