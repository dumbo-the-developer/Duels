package com.meteordevelopments.duels.listeners;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.api.event.match.MatchStartEvent;
import com.meteordevelopments.duels.api.event.match.MatchEndEvent;
import com.meteordevelopments.duels.arena.ArenaImpl;
import com.meteordevelopments.duels.arena.ArenaManagerImpl;
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

import java.lang.ref.WeakReference;
import java.util.*;

/**
 * Prevents players throwing an enderpearl before entering a duel and teleporting out with kit items.
 */
public class EnderpearlListener implements Listener {

    private static final long PEARL_EXPIRY = 60 * 1000L;

    private final ArenaManagerImpl arenaManager;

    // Maps an enderpearl thrower to enderpearls thrown (before entering a match).
    private final Multimap<UUID, Pearl> pearls = HashMultimap.create();
    private final Map<ArenaImpl, Set<Pearl>> arenaPearls = new HashMap<>();

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

        if (!(enderPearl.getShooter() instanceof Player player)) {
            return;
        }

        final ArenaImpl arena = arenaManager.get(player);
        if (arena != null) {
            arenaPearls.computeIfAbsent(arena, k -> new HashSet<>()).add(new Pearl(enderPearl));
            return;
        }

        removeExpired(player);
        pearls.put(player.getUniqueId(), new Pearl(enderPearl));
    }

    @EventHandler
    public void on(final ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof EnderPearl enderPearl)) {
            return;
        }

        if (!(enderPearl.getShooter() instanceof Player player)) {
            return;
        }

        final Collection<Pearl> pearls = this.pearls.asMap().get(player.getUniqueId());

        if (pearls != null && !pearls.isEmpty()) {
            final Iterator<Pearl> iterator = pearls.iterator();

            while (iterator.hasNext()) {
                final Pearl pearl = iterator.next();

                if (enderPearl.equals(pearl.pearl.get())) {
                    iterator.remove();
                    break;
                }
            }
        }

        final ArenaImpl arena = arenaManager.get(player);
        if (arena != null) {
            final Set<Pearl> arenaPearls = this.arenaPearls.get(arena);
            if (arenaPearls != null) {
                arenaPearls.removeIf(pearl -> enderPearl.equals(pearl.pearl.get()));
            }
        }
    }

    @EventHandler
    public void on(final MatchStartEvent event) {
        final ArenaImpl arena = (ArenaImpl) event.getMatch().getArena();
        final Set<Pearl> existingPearls = arenaPearls.get(arena);

        if (existingPearls != null) {
            existingPearls.forEach(pearl -> {
                final EnderPearl enderPearl = pearl.pearl.get();
                if (enderPearl != null && !enderPearl.isDead()) {
                    enderPearl.remove();
                }
            });
            existingPearls.clear();
        } else {
            arenaPearls.put(arena, new HashSet<>());
        }

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

    @EventHandler
    public void on(final MatchEndEvent event) {
        final ArenaImpl arena = (ArenaImpl) event.getMatch().getArena();
        final Set<Pearl> pearls = arenaPearls.get(arena);

        if (pearls != null && !pearls.isEmpty()) {
            final Set<Pearl> pearlsCopy = new HashSet<>(pearls);
            pearls.clear();

            pearlsCopy.forEach(pearl -> {
                final EnderPearl enderPearl = pearl.pearl.get();

                if (enderPearl != null && !enderPearl.isDead()) {
                    enderPearl.remove();
                }
            });
        }
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
