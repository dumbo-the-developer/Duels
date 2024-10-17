package com.meteordevelopments.duels.listeners;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.arena.ArenaImpl;
import com.meteordevelopments.duels.arena.ArenaManagerImpl;
import com.meteordevelopments.duels.kit.KitImpl;
import com.meteordevelopments.duels.util.EventUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * Overrides damage cancellation by other plugins for players in a duel.
 */
public class DamageListener implements Listener {

    private final ArenaManagerImpl arenaManager;

    public DamageListener(final DuelsPlugin plugin) {
        this.arenaManager = plugin.getArenaManager();

        if (plugin.getConfiguration().isForceAllowCombat()) {
            plugin.doSyncAfter(() -> Bukkit.getPluginManager().registerEvents(this, plugin), 1L);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(final EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();
        final Player damager = EventUtil.getDamager(event);

        if (damager == null) {
            return;
        }

        final ArenaImpl arena = arenaManager.get(player);

        // Only activate when winner is undeclared
        if (arena == null || !arenaManager.isInMatch(damager) || arena.isEndGame()) {
            return;
        }

        if (arena.getMatch().getKit() != null) {
            KitImpl.Characteristic characteristic = arena.getMatch().getKit().getCharacteristics().stream().filter(
                    c -> c == KitImpl.Characteristic.BOXING).findFirst().orElse(null);
            if(characteristic != null) {
                if(arena.getMatch().getHits(damager) >= 100) {
                    player.damage(player.getMaxHealth());
                    return;
                }
                event.setDamage(0);
                return;
            }
        }

        arena.getMatch().addDamageToPlayer(damager, event.getFinalDamage());

        if(!event.isCancelled()) return;

        event.setCancelled(false);
    }
}