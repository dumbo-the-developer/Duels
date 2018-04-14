package me.realized.duels.cache;

import java.util.HashMap;
import java.util.Map;
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

    protected abstract V instance(final Player player);

    public V get(final Player player) {
        return cache.computeIfAbsent(player.getUniqueId(), result -> instance(player));
    }

    public void invalidate(final Player player) {
        cache.remove(player.getUniqueId());
    }
}
