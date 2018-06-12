package me.realized.duels.player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.util.Loadable;
import org.bukkit.entity.Player;

public class PlayerInfoManager implements Loadable {

    private final Map<UUID, PlayerInfo> cache = new HashMap<>();

    public PlayerInfoManager(final DuelsPlugin plugin) {}

    @Override
    public void handleLoad() {}

    @Override
    public void handleUnload() {}

    public PlayerInfo get(final Player player) {
        return cache.get(player.getUniqueId());
    }

    public void put(final Player player, final PlayerInfo info) {
        cache.put(player.getUniqueId(), info);
    }

    public void remove(final Player player) {
        cache.remove(player.getUniqueId());
    }

    public PlayerInfo removeAndGet(final Player player) {
        final PlayerInfo info = get(player);
        remove(player);
        return info;
    }
}
