package me.realized.duels.request;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.api.event.request.RequestSendEvent;
import me.realized.duels.config.Config;
import me.realized.duels.setting.Settings;
import me.realized.duels.util.Loadable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class RequestManager implements Loadable, Listener {

    private final DuelsPlugin plugin;
    private final Config config;
    private final Map<UUID, Map<UUID, Request>> requests = new HashMap<>();

    public RequestManager(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void handleLoad() {}

    @Override
    public void handleUnload() {
        requests.clear();
    }

    private Map<UUID, Request> get(final Player player, final boolean create) {
        Map<UUID, Request> cached = requests.get(player.getUniqueId());

        if (cached == null && create) {
            requests.put(player.getUniqueId(), cached = new HashMap<>());
            return cached;
        }

        return cached;
    }

    public boolean send(final Player sender, final Player target, final Settings setting) {
        final Request request = new Request(sender, target, setting);
        final RequestSendEvent event = new RequestSendEvent(sender, target, request);
        plugin.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return false;
        }

        get(sender, true).put(target.getUniqueId(), request);
        return true;
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

        if (System.currentTimeMillis() - request.getCreation() >= config.getExpiration() * 1000L) {
            cached.remove(target.getUniqueId());
            return false;
        }

        return true;
    }

    public Request remove(final Player sender, final Player target) {
        final Map<UUID, Request> cached = get(sender, false);

        if (cached == null) {
            return null;
        }

        final Request request = cached.remove(target.getUniqueId());

        if (request == null) {
            return null;
        }

        if (System.currentTimeMillis() - request.getCreation() >= config.getExpiration() * 1000L) {
            cached.remove(target.getUniqueId());
            return null;
        }

        return request;
    }

    @EventHandler
    public void on(final PlayerQuitEvent event) {
        requests.remove(event.getPlayer().getUniqueId());
    }
}
