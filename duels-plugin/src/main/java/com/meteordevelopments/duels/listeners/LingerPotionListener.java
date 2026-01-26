package com.meteordevelopments.duels.listeners;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.arena.ArenaImpl;
import com.meteordevelopments.duels.arena.ArenaManagerImpl;
import com.meteordevelopments.duels.match.team.TeamDuelMatch;
import com.meteordevelopments.duels.spectate.SpectateManagerImpl;
import com.meteordevelopments.duels.util.compat.CompatUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;

/**
 * Prevents spectators from being affected by lingering potions.
 */
public class LingerPotionListener {

    private final ArenaManagerImpl arenaManager;
    private final SpectateManagerImpl spectateManager;

    public LingerPotionListener(final DuelsPlugin plugin) {
        this.arenaManager = plugin.getArenaManager();
        this.spectateManager = plugin.getSpectateManager();

        // Lingering potions were released in MC 1.9
        if (CompatUtil.isPre1_9()) {
            return;
        }

        Bukkit.getPluginManager().registerEvents(new Post1_9Listener(), plugin);
    }

    public class Post1_9Listener implements Listener {

        @EventHandler
        public void on(final AreaEffectCloudApplyEvent event) {
            if (!(event.getEntity().getSource() instanceof Player)) {
                return;
            }

            final Player source = (Player) event.getEntity().getSource();

            final ArenaImpl arena = arenaManager.get(source);
            if (arena == null || !(arena.getMatch() instanceof TeamDuelMatch teamMatch)) {
                // Still remove spectators regardless
                event.getAffectedEntities().removeIf(entity -> entity instanceof Player && spectateManager.isSpectating((Player) entity));
                return;
            }

            final TeamDuelMatch.Team sourceTeam = teamMatch.getPlayerToTeam().get(source);

            // Remove spectators and teammates from harmful lingering clouds
            event.getAffectedEntities().removeIf(entity -> {
                if (entity instanceof Player p) {
                    if (spectateManager.isSpectating(p)) {
                        return true;
                    }
                    final TeamDuelMatch.Team affectedTeam = teamMatch.getPlayerToTeam().get(p);
                    if (affectedTeam != null && affectedTeam.equals(sourceTeam)) {
                        // Only filter for harmful clouds; check effects on cloud
                        boolean harmful = false;
                        // Check custom effects
                        for (org.bukkit.potion.PotionEffect eff : event.getEntity().getCustomEffects()) {
                            final org.bukkit.potion.PotionEffectType t = eff.getType();
                            if (t.equals(org.bukkit.potion.PotionEffectType.POISON) ||
                                t.equals(org.bukkit.potion.PotionEffectType.HARM) ||
                                t.equals(org.bukkit.potion.PotionEffectType.WITHER) ||
                                t.equals(org.bukkit.potion.PotionEffectType.WEAKNESS) ||
                                t.equals(org.bukkit.potion.PotionEffectType.SLOW) ||
                                t.equals(org.bukkit.potion.PotionEffectType.SLOW_DIGGING) ||
                                t.equals(org.bukkit.potion.PotionEffectType.BLINDNESS) ||
                                t.equals(org.bukkit.potion.PotionEffectType.CONFUSION) ||
                                t.equals(org.bukkit.potion.PotionEffectType.HUNGER) ||
                                t.equals(org.bukkit.potion.PotionEffectType.LEVITATION) ||
                                t.equals(org.bukkit.potion.PotionEffectType.UNLUCK) ||
                                t.equals(org.bukkit.potion.PotionEffectType.BAD_OMEN) ||
                                t.equals(org.bukkit.potion.PotionEffectType.DARKNESS)) {
                                harmful = true;
                                break;
                            }
                        }
                        // Also check base potion type
                        if (!harmful) {
                            final org.bukkit.potion.PotionType base = event.getEntity().getBasePotionData().getType();
                            if (base == org.bukkit.potion.PotionType.POISON ||
                                base == org.bukkit.potion.PotionType.INSTANT_DAMAGE ||
                                base == org.bukkit.potion.PotionType.SLOWNESS ||
                                base == org.bukkit.potion.PotionType.WEAKNESS) {
                                harmful = true;
                            }
                        }
                        return harmful;
                    }
                }
                return false;
            });
        }
    }
}
