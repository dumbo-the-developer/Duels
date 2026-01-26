package com.meteordevelopments.duels.listeners;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.arena.ArenaImpl;
import com.meteordevelopments.duels.arena.ArenaManagerImpl;
import com.meteordevelopments.duels.config.Lang;
import com.meteordevelopments.duels.match.team.TeamDuelMatch;
import com.meteordevelopments.duels.util.EventUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

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

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTeamMinecartDirectDamage(final EntityDamageByEntityEvent event) {
        // Some servers still deliver damage with ExplosiveMinecart as damager
        if (!(event.getEntity() instanceof Player damaged)) {
            return;
        }
        if (!(event.getDamager() instanceof ExplosiveMinecart minecart)) {
            return;
        }

        final ArenaImpl arena = arenaManager.get(damaged);
        if (arena == null || !(arena.getMatch() instanceof TeamDuelMatch teamMatch)) {
            return;
        }

        java.util.UUID ownerUuid = com.meteordevelopments.duels.listeners.ExplosionOwnershipListener.getTntMinecartOwner(minecart);
        if (ownerUuid == null && com.meteordevelopments.duels.listeners.ExplosionOwnershipListener.isRecentMinecartExplosionInWorld(damaged.getWorld(), 2000)) {
            ownerUuid = com.meteordevelopments.duels.listeners.ExplosionOwnershipListener.getRecentMinecartExplosionOwner(damaged.getWorld());
        }
        if (ownerUuid == null) {
            return;
        }
        final Player owner = damaged.getServer().getPlayer(ownerUuid);
        if (owner == null || !arenaManager.isInMatch(owner)) {
            return;
        }

        final TeamDuelMatch.Team damagerTeam = teamMatch.getPlayerToTeam().get(owner);
        final TeamDuelMatch.Team damagedTeam = teamMatch.getPlayerToTeam().get(damaged);
        if (damagerTeam != null && damagerTeam.equals(damagedTeam)) {
            event.setCancelled(true);
            lang.sendMessage(owner, "ERROR.party.cannot-friendly-fire", "name", damaged.getName());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTeamBlockExplosion(final EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (event.getCause() != EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) {
            return;
        }

        final ArenaImpl arena = arenaManager.get(player);
        if (arena == null) {
            return;
        }

        // Check recent trigger in this world (beds/anchors)
        if (!com.meteordevelopments.duels.listeners.ExplosionOwnershipListener.isRecentBlockExplosionInWorld(player.getWorld(), 1500)) {
            return;
        }

        final java.util.UUID trigger = com.meteordevelopments.duels.listeners.ExplosionOwnershipListener.getRecentBlockExplosionTrigger(player.getWorld());
        if (trigger == null) {
            return;
        }

        final Player damager = player.getServer().getPlayer(trigger);
        if (damager == null || !arenaManager.isInMatch(damager)) {
            return;
        }

        if (!(arena.getMatch() instanceof TeamDuelMatch teamMatch)) {
            return;
        }

        final TeamDuelMatch.Team damagerTeam = teamMatch.getPlayerToTeam().get(damager);
        final TeamDuelMatch.Team damagedTeam = teamMatch.getPlayerToTeam().get(player);

        if (damagerTeam != null && damagerTeam.equals(damagedTeam)) {
            event.setCancelled(true);
            lang.sendMessage(damager, "ERROR.party.cannot-friendly-fire", "name", player.getName());
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onTeamEntityExplosion(final EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
            return;
        }
        if (!(event.getEntity() instanceof Player damaged)) {
            return;
        }
        final ArenaImpl arena = arenaManager.get(damaged);
        if (arena == null || !(arena.getMatch() instanceof TeamDuelMatch teamMatch)) {
            return;
        }

        // This handler covers TNT minecart explosions that don't trigger EntityDamageByEntityEvent
        // or where the minecart entity is removed before damage is applied
        if (!com.meteordevelopments.duels.listeners.ExplosionOwnershipListener.isRecentMinecartExplosionInWorld(damaged.getWorld(), 2000)) {
            return;
        }
        final java.util.UUID ownerUuid = com.meteordevelopments.duels.listeners.ExplosionOwnershipListener.getRecentMinecartExplosionOwner(damaged.getWorld());
        if (ownerUuid == null) {
            return;
        }
        final Player owner = damaged.getServer().getPlayer(ownerUuid);
        if (owner == null || !arenaManager.isInMatch(owner)) {
            return;
        }

        final TeamDuelMatch.Team damagerTeam = teamMatch.getPlayerToTeam().get(owner);
        final TeamDuelMatch.Team damagedTeam = teamMatch.getPlayerToTeam().get(damaged);
        if (damagerTeam != null && damagerTeam.equals(damagedTeam)) {
            event.setCancelled(true);
            lang.sendMessage(owner, "ERROR.party.cannot-friendly-fire", "name", damaged.getName());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTeamPotionSplash(final PotionSplashEvent event) {
        final ThrownPotion potion = event.getPotion();
        if (!(potion.getShooter() instanceof Player thrower)) {
            return;
        }

        final ArenaImpl arena = arenaManager.get(thrower);
        if (arena == null || !(arena.getMatch() instanceof TeamDuelMatch teamMatch)) {
            return;
        }

        final TeamDuelMatch.Team throwerTeam = teamMatch.getPlayerToTeam().get(thrower);
        if (throwerTeam == null) {
            return;
        }

        // Check if potion has harmful effects
        boolean hasHarmfulEffect = false;
        for (PotionEffect effect : potion.getEffects()) {
            PotionEffectType type = effect.getType();
            // Check for harmful effects
            if (type.equals(PotionEffectType.POISON) || 
                type.equals(PotionEffectType.HARM) ||
                type.equals(PotionEffectType.WITHER) ||
                type.equals(PotionEffectType.WEAKNESS) ||
                type.equals(PotionEffectType.SLOW) ||
                type.equals(PotionEffectType.SLOW_DIGGING) ||
                type.equals(PotionEffectType.BLINDNESS) ||
                type.equals(PotionEffectType.CONFUSION) ||
                type.equals(PotionEffectType.HUNGER) ||
                type.equals(PotionEffectType.LEVITATION) ||
                type.equals(PotionEffectType.UNLUCK) ||
                type.equals(PotionEffectType.BAD_OMEN) ||
                type.equals(PotionEffectType.DARKNESS)) {
                hasHarmfulEffect = true;
                break;
            }
        }

        if (!hasHarmfulEffect) {
            return;
        }

        // Zero out intensity for teammates so no effect applies
        for (org.bukkit.entity.LivingEntity entity : event.getAffectedEntities()) {
            if (entity instanceof Player affected) {
                final TeamDuelMatch.Team affectedTeam = teamMatch.getPlayerToTeam().get(affected);
                if (affectedTeam != null && affectedTeam.equals(throwerTeam)) {
                    event.setIntensity(entity, 0.0);
                    lang.sendMessage(thrower, "ERROR.party.cannot-friendly-fire", "name", affected.getName());
                }
            }
        }
    }
}
