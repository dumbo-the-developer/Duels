package me.realized.duels.hooks;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.arena.ArenaManager;
import me.realized.duels.util.hook.PluginHook;
import net.Indyuce.bh.api.BountyClaimEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class BountyHuntersHook extends PluginHook<DuelsPlugin> implements Listener {

    private final ArenaManager arenaManager;

    public BountyHuntersHook(final DuelsPlugin plugin) {
        super(plugin, "BountyHunters");
        this.arenaManager = plugin.getArenaManager();

        if (plugin.getConfiguration().isPreventBountyLoss()) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void on(final BountyClaimEvent event) {
        if (!arenaManager.isInMatch(event.getClaimer())) {
            return;
        }

        event.setCancelled(true);
    }
}
