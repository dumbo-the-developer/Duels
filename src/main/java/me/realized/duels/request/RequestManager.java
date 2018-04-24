package me.realized.duels.request;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.cache.Setting;
import me.realized.duels.util.Loadable;
import org.bukkit.entity.Player;

public class RequestManager implements Loadable {

    private final DuelsPlugin plugin;
    private final Map<UUID, Map<UUID, Request>> requests = new HashMap<>();

    public RequestManager(final DuelsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void handleLoad() throws Exception {

    }

    @Override
    public void handleUnload() throws Exception {

    }

    private Map<UUID, Request> get(final Player player, final boolean create) {
        Map<UUID, Request> cached = requests.get(player.getUniqueId());

        if (cached == null && create) {
            requests.put(player.getUniqueId(), cached = new HashMap<>());
            return cached;
        }

        return cached;
    }

    public void send(final Player sender, final Player target, final Setting setting) {
        get(sender, true).put(target.getUniqueId(), new Request(target, setting));
    }

    public boolean has(final Player sender, final Player target) {
        final Map<UUID, Request> cached = get(sender, false);

        if (cached == null) {
            return false;
        }

        final Request request = cached.get(target.getUniqueId());

        if (request == null) {
            return false;
        }

        // add expiry check
        return true;
    }

    public Request remove(final Player sender, final Player target) {
        final Map<UUID, Request> cached = get(sender, false);

        if (cached == null) {
            return null;
        }

        return cached.remove(target.getUniqueId());
    }
}
