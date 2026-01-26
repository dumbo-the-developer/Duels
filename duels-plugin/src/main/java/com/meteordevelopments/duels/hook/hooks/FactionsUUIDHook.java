package com.meteordevelopments.duels.hook.hooks;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.arena.ArenaManagerImpl;
import com.meteordevelopments.duels.config.Config;
import com.meteordevelopments.duels.util.Log;
import com.meteordevelopments.duels.util.hook.PluginHook;
import com.meteordevelopments.duels.util.reflect.ReflectionUtil;
import dev.kitteh.factions.event.PowerLossEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class FactionsUUIDHook extends PluginHook<DuelsPlugin> {

    public static final String NAME = "FactionsUUID";

    private final Config config;
    private final ArenaManagerImpl arenaManager;

    public FactionsUUIDHook(final DuelsPlugin plugin) {
        super(plugin, NAME);
        this.config = plugin.getConfiguration();
        this.arenaManager = plugin.getArenaManager();

        Listener listener = null;

        if (ReflectionUtil.getClassUnsafe("dev.kitteh.factions.event.PowerLossEvent") != null) {
            listener = new FactionsUUIDListener();
        }

        if (listener == null) {
            Log.error("Could not detect this version of Factions. Please contact the developer if you believe this is an error.");
            return;
        }

        Bukkit.getPluginManager().registerEvents(listener, plugin);
    }

    public class FactionsUUIDListener implements Listener {

        @EventHandler
        public void on(final PowerLossEvent event) {
            if (!config.isFuNoPowerLoss()) {
                return;
            }

            final Player player = event.getFPlayer().asPlayer();

            if (player == null || !arenaManager.isInMatch(player)) {
                return;
            }

            event.setCancelled(true);
        }
    }
}
