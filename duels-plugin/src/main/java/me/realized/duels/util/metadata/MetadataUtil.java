package me.realized.duels.util.metadata;

import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

public final class MetadataUtil {

    private MetadataUtil() {}

    public static Object get(final Plugin plugin, final Player player, final String key) {
        return player.getMetadata(key).stream().filter(value -> value.getOwningPlugin().equals(plugin)).findFirst().map(MetadataValue::value).orElse(null);
    }

    public static void put(final Plugin plugin, final Player player, final String key, final Object data) {
        player.setMetadata(key, new FixedMetadataValue(plugin, data));
    }

    public static void remove(final Plugin plugin, final Player player, final String key) {
        player.removeMetadata(key, plugin);
    }

    public static Object removeAndGet(final Plugin plugin, final Player player, final String key) {
        final Object value = get(plugin, player, key);
        remove(plugin, player, key);
        return value;
    }
}
