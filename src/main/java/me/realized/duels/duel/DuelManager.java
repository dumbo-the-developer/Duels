package me.realized.duels.duel;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.util.Loadable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class DuelManager implements Loadable, Listener {

    private final DuelsPlugin plugin;

    public DuelManager(final DuelsPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void handleLoad() throws Exception {

    }

    @Override
    public void handleUnload() throws Exception {

    }

    @EventHandler
    public void on(final EntityDamageByEntityEvent event) {

    }

    @EventHandler
    public void on(final PlayerQuitEvent event) {

    }
}
