package me.realized.duels.hook.hooks;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.arena.ArenaManagerImpl;
import me.realized.duels.config.Config;
import me.realized.duels.util.hook.PluginHook;
import net.Indyuce.bountyhunters.api.event.BountyClaimEvent;
import net.Indyuce.bountyhunters.api.event.BountyCreateEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class BountyHuntersHook extends PluginHook<DuelsPlugin> implements Listener {

    public static final String NAME = "BountyHunters";

    private final Config config;
    private final ArenaManagerImpl arenaManager;

    public BountyHuntersHook(final DuelsPlugin plugin) {
        super(plugin, NAME);
        this.config = plugin.getConfiguration();
        this.arenaManager = plugin.getArenaManager();

        try {
            Class.forName("net.Indyuce.bountyhunters.api.event.BountyClaimEvent");
            Class.forName("net.Indyuce.bountyhunters.api.event.BountyCreateEvent");
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("This version of " + getName() + " is not supported. Please try upgrading to the latest version.");
        }

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true)
    public void on(final BountyClaimEvent event) {
        if (!config.isPreventBountyLoss() || !arenaManager.isInMatch(event.getClaimer())) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void on(final BountyCreateEvent event) {
        final Player target = event.getBounty().getTarget().getPlayer();

        if (!config.isPreventBountyLoss() || target == null || !arenaManager.isInMatch(target)) {
            return;
        }

        event.setCancelled(true);
    }
}
