package me.realized.duels.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import me.realized.duels.util.Loadable;
import org.bukkit.entity.Player;

public abstract class Cache<V> implements Loadable {

    private final Map<UUID, V> cache = new HashMap<>();

    @Override
    public void handleLoad() {}

    @Override
    public void handleUnload() {
        cache.clear();
    }

    abstract V create(final Player player);

    public Optional<V> get(final Player player) {
        return Optional.ofNullable(cache.get(player.getUniqueId()));
    }

    public V getSafely(final Player player) {
        return cache.computeIfAbsent(player.getUniqueId(), result -> create(player));
    }

    public void put(final Player player) {
        cache.put(player.getUniqueId(), create(player));
    }

    public Optional<V> remove(final Player player) {
        return Optional.ofNullable(cache.remove(player.getUniqueId()));
    }
}
