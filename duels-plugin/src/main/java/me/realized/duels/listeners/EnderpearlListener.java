package me.realized.duels.listeners;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.api.event.match.MatchStartEvent;
import me.realized.duels.arena.ArenaManagerImpl;
import org.bukkit.Bukkit;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Prevents players throwing an enderpearl before entering a duel and teleporting out with kit items.
 */
public class EnderpearlListener implements Listener {

    private static final long PEARL_EXPIRY = 60 * 1000L;

    private final ArenaManagerImpl arenaManager;

    // Maps an enderpearl thrower to enderpearls thrown.
    private final Multimap<UUID, Pearl> pearls = HashMultimap.create();

    public EnderpearlListener(final DuelsPlugin plugin) {
        this.arenaManager = plugin.getArenaManager();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private void removeExpired(final Player player) {
        final Collection<Pearl> pearls = this.pearls.asMap().get(player.getUniqueId());

        if (pearls == null || pearls.isEmpty()) {
            return;
        }

        final long now = System.currentTimeMillis();
        pearls.removeIf(pearl -> now - pearl.creation > PEARL_EXPIRY);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void on(final ProjectileLaunchEvent event) {
        if (event.getEntityType() != EntityType.ENDER_PEARL) {
            return;
        }

        final EnderPearl enderPearl = (EnderPearl) event.getEntity();

        if (!(enderPearl.getShooter() instanceof Player)) {
            return;
        }

        final Player player = (Player) enderPearl.getShooter();

        // Ignore pearls thrown in match
        if (arenaManager.isInMatch(player)) {
            return;
        }

        removeExpired(player);
        pearls.put(player.getUniqueId(), new Pearl(enderPearl));
    }

    @EventHandler
    public void on(final ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof EnderPearl)) {
            return;
        }

        final EnderPearl enderPearl = (EnderPearl) event.getEntity();

        if (!(enderPearl.getShooter() instanceof Player)) {
            return;
        }

        final Collection<Pearl> pearls = this.pearls.asMap().get(((Player) enderPearl.getShooter()).getUniqueId());

        if (pearls == null || pearls.isEmpty()) {
            return;
        }

        final Iterator<Pearl> iterator = pearls.iterator();

        while (iterator.hasNext()) {
            final Pearl pearl = iterator.next();

            if (enderPearl.equals(pearl.pearl.get())) {
                iterator.remove();
                break;
            }
        }
    }

    @EventHandler
    public void on(final MatchStartEvent event) {
        for (final Player player : event.getPlayers()) {
            final Collection<Pearl> pearls = this.pearls.asMap().remove(player.getUniqueId());

            if (pearls == null || pearls.isEmpty()) {
                continue;
            }

            pearls.forEach(pearl -> {
                final EnderPearl enderPearl = pearl.pearl.get();

                if (enderPearl != null && !enderPearl.isDead()) {
                    enderPearl.remove();
                }
            });
        }
    }

    @EventHandler
    public void on(final PlayerQuitEvent event) {
        pearls.asMap().remove(event.getPlayer().getUniqueId());
    }

    private static class Pearl {

        private final long creation;
        private final WeakReference<EnderPearl> pearl;

        public Pearl(final EnderPearl pearl) {
            this.creation = System.currentTimeMillis();
            this.pearl = new WeakReference<>(pearl);
        }
    }
}
