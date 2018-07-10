package me.realized.duels.request;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.api.event.request.RequestSendEvent;
import me.realized.duels.config.Config;
import me.realized.duels.config.Lang;
import me.realized.duels.setting.Settings;
import me.realized.duels.util.Loadable;
import me.realized.duels.util.TextBuilder;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class RequestManager implements Loadable, Listener {

    private final DuelsPlugin plugin;
    private final Config config;
    private final Lang lang;
    private final Map<UUID, Map<UUID, Request>> requests = new HashMap<>();

    public RequestManager(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.lang = plugin.getLang();
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

    public void send(final Player sender, final Player target, final Settings settings) {
        final Request request = new Request(sender, target, settings);
        final RequestSendEvent event = new RequestSendEvent(sender, target, request);
        plugin.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        get(sender, true).put(target.getUniqueId(), request);
        final String kit = settings.getKit() != null ? settings.getKit().getName() : "Not Selected";
        final String arena = settings.getArena() != null ? settings.getArena().getName() : "Random";
        final int betAmount = settings.getBet();
        final String itemBetting = settings.isItemBetting() ? "&aenabled" : "&cdisabled";

        lang.sendMessage(sender, "COMMAND.duel.request.send.sender",
            "name", target.getName(), "kit", kit, "arena", arena, "bet_amount", betAmount, "item_betting", itemBetting);
        lang.sendMessage(target, "COMMAND.duel.request.send.receiver",
            "name", sender.getName(), "kit", kit, "arena", arena, "bet_amount", betAmount, "item_betting", itemBetting);

        final String path = "COMMAND.duel.request.send.clickable-text.";

        TextBuilder
            .of(lang.getMessage(path + "info"))
            .add(lang.getMessage(path + "accept"), Action.RUN_COMMAND, "/duel accept " + sender.getName())
            .add(lang.getMessage(path + "deny"), Action.RUN_COMMAND, "/duel deny " + sender.getName())
            .send(target);
        TextBuilder.of(lang.getMessage(path + "extra")).send(target);
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
