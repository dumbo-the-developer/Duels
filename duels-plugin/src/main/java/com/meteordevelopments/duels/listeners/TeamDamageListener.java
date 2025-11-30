package com.meteordevelopments.duels.listeners;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.arena.ArenaImpl;
import com.meteordevelopments.duels.arena.ArenaManagerImpl;
import com.meteordevelopments.duels.config.Lang;
import com.meteordevelopments.duels.match.team.TeamDuelMatch;
import com.meteordevelopments.duels.util.EventUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * Prevents friendly fire in team matches when force-allow-combat is false.
 */
public class TeamDamageListener implements Listener {

    private final ArenaManagerImpl arenaManager;
    private final Lang lang;

    public TeamDamageListener(final DuelsPlugin plugin) {
        this.arenaManager = plugin.getArenaManager();

        if (!plugin.getConfiguration().isForceAllowCombat()) {
            Bukkit.getPluginManager().registerEvents(this, plugin);
        }
        this.lang = plugin.getLang();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTeamDamage(final EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        final Player damaged = (Player) event.getEntity();
        final Player damager = EventUtil.getDamager(event);

        if (damager == null) {
            return;
        }

        final ArenaImpl arena = arenaManager.get(damaged);

        // Check if both players are in a match
        if (arena == null || !arenaManager.isInMatch(damager)) {
            return;
        }

        // Check if this is a team match
        if (!(arena.getMatch() instanceof TeamDuelMatch)) {
            return;
        }

        final TeamDuelMatch teamMatch = (TeamDuelMatch) arena.getMatch();

        // Get teams for both players
        final TeamDuelMatch.Team damagerTeam = teamMatch.getPlayerToTeam().get(damager);
        final TeamDuelMatch.Team damagedTeam = teamMatch.getPlayerToTeam().get(damaged);

        // If both players are on the same team, cancel the damage
        if (damagerTeam != null && damagerTeam.equals(damagedTeam)) {
            event.setCancelled(true);
            lang.sendMessage(damager, "ERROR.party.cannot-friendly-fire", "name", damaged.getName());
        }
    }
}
