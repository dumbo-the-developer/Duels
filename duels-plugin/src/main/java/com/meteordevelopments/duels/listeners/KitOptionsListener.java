package com.meteordevelopments.duels.listeners;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.api.event.match.MatchEndEvent;
import com.meteordevelopments.duels.api.event.match.MatchStartEvent;
import com.meteordevelopments.duels.arena.ArenaImpl;
import com.meteordevelopments.duels.arena.ArenaManagerImpl;
import com.meteordevelopments.duels.countdown.DuelCountdown;
import com.meteordevelopments.duels.duel.DuelManager;
import com.meteordevelopments.duels.match.DuelMatch;
import com.meteordevelopments.duels.config.Config;
import com.meteordevelopments.duels.kit.KitImpl.Characteristic;
import com.meteordevelopments.duels.util.PlayerUtil;
import com.meteordevelopments.duels.util.compat.CompatUtil;
import com.meteordevelopments.duels.util.compat.Items;
import com.meteordevelopments.duels.util.metadata.MetadataUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

/**
 * Applies kit characteristics (options) to duels.
 */
public class KitOptionsListener implements Listener {

    private static final String METADATA_KEY = "Duels-MaxNoDamageTicks";

    private final DuelsPlugin plugin;
    private final Config config;
    private final ArenaManagerImpl arenaManager;
    private final DuelManager duelManager;

    public KitOptionsListener(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.arenaManager = plugin.getArenaManager();
        this.duelManager = plugin.getDuelManager();

        Bukkit.getPluginManager().registerEvents(this, plugin);
        Bukkit.getPluginManager().registerEvents(CompatUtil.isPre1_14() ? new ComboPre1_14Listener() : new ComboPost1_14Listener(), plugin);
    }

    private boolean isEnabled(final ArenaImpl arena, final Characteristic characteristic) {
        final DuelMatch match = arena.getMatch();
        return match != null && match.getKit() != null && match.getKit().hasCharacteristic(characteristic);
    }

    @EventHandler
    public void on(final EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        final ArenaImpl arena = arenaManager.get(player);

        if (arena == null) {
            return;
        }

        final DuelMatch match = arena.getMatch();
        if (match == null) {
            return;
        }

        // For ROUNDS3, if damage would kill the player, handle round end
        if (isEnabled(arena, Characteristic.ROUNDS3)) {
            double finalHealth = player.getHealth() - event.getFinalDamage();
            if (finalHealth <= 0) {
                // Cancel the damage event immediately to prevent any delayed damage
                event.setCancelled(true);

                // Find the winner (the other player)
                Player winner = match.getAlivePlayers().stream()
                        .filter(p -> !p.equals(player))
                        .findFirst()
                        .orElse(null);

                if (winner != null) {
                    // Add round win
                    match.addRoundWin(winner);

                    if (match.hasWonMatch(winner)) {
                        // On final round, let the damage go through to kill the player
                        match.markAsDead(player);
                        // Let DuelManager handle the match end with all effects
                        // Pass the winner's current health for the death message
                        double winnerHealth = Math.ceil(winner.getHealth()) * 0.5;
                        arena.broadcast(plugin.getLang().getMessage("DUEL.on-death.with-killer",
                                "name", player.getName(),
                                "killer", winner.getName(),
                                "health", winnerHealth));
                        duelManager.handleMatchEnd(match, arena, player, player.getLocation(), winner);
                        return;
                    }

                    // Cancel the damage event for non-final rounds
                    event.setDamage(0);
                    player.setHealth(player.getMaxHealth());

                    // Start next round
                    match.nextRound();

                    // Reset both players' health and equipment
                    for (Player p : match.getAllPlayers()) {
                        PlayerUtil.reset(p);
                        p.setHealth(p.getMaxHealth());
                        p.setNoDamageTicks(40); // Give 2 seconds immunity to prevent damage carry-over
                        if (match.getKit() != null) {
                            match.getKit().equip(p);
                        }
                    }

                    // Use the plugin's teleport system for both players
                    Player[] players = match.getAllPlayers().toArray(new Player[0]);
                    if (players.length >= 2) {
                        plugin.getTeleport().tryTeleport(players[0], arena.getPosition(1));
                        plugin.getTeleport().tryTeleport(players[1], arena.getPosition(2));
                    }

                    // Broadcast round end and status
                    arena.broadcast(plugin.getLang().getMessage("DUEL.rounds.round-end",
                            "round", match.getCurrentRound() - 1,
                            "winner", winner.getName()));

                    arena.broadcast(plugin.getLang().getMessage("DUEL.rounds.round-status",
                            "player1", players[0].getName(),
                            "wins1", match.getRoundWins(players[0]),
                            "player2", players[1].getName(),
                            "wins2", match.getRoundWins(players[1])));

                    // Check for match point
                    if (match.getRoundWins(winner) == 1) {
                        arena.broadcast(plugin.getLang().getMessage("DUEL.rounds.match-point",
                                "player", winner.getName()));
                    }

                    // Start countdown and announce new round
                    arena.broadcast(plugin.getLang().getMessage("DUEL.rounds.round-start",
                            "round", match.getCurrentRound()));

                    DuelCountdown countdown = new DuelCountdown(plugin, arena, match);
                    arena.setCountdown(countdown);
                    countdown.startCountdown(0L, 20L);
                }
                return;
            }
        }

        if (!isEnabled(arena, Characteristic.SUMO) && !isEnabled(arena, Characteristic.BOXING)) {
            return;
        }

        event.setDamage(0);
    }

    @EventHandler
    public void on(final EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        final ArenaImpl arena = arenaManager.get(player);

        if (arena == null || !isEnabled(arena, Characteristic.LOKA)) {
            return;
        }
        double originalDamage = event.getDamage();
        double increaseDamage = originalDamage * 1.33;
        event.setDamage(increaseDamage);
    }

    @EventHandler
    public void on(final FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        final ArenaImpl arena = arenaManager.get(player);

        if (arena == null || !isEnabled(arena, Characteristic.HUNGER)) {
            return;
        }

        event.setCancelled(true);
    }

//    private void handleExplosion(List<Block> blocks, Cancellable event, Player player) {
//        if (player == null) {
//            return;
//        }
//
//        ArenaImpl arena = arenaManager.get(player);
//
//        if (arena == null || arena.getMatch() == null) {
//            return;
//        }
//
//        for (Block block : blocks) {
//            if (arena.getMatch().placedBlocks.contains(block)) {
//                continue;
//            }
//
//            if (isEnabled(arena, Characteristic.BREAK)) {
//                if (!arena.getMatch().brokenBlocks.containsKey(block.getLocation())) {
//                    arena.getMatch().brokenBlocks.put(block.getLocation(), block.getBlockData().clone());
//                }
//            } else {
//                event.setCancelled(true);
//            }
//        }
//    }

    private Player findClosestPlayer(Location location) {
        double radius = 10;
        Player closestPlayer = null;
        double closestDistance = radius;

        for (Player player : Objects.requireNonNull(location.getWorld()).getPlayers()) {
            double distance = player.getLocation().distance(location);
            if (distance < closestDistance) {
                closestPlayer = player;
                closestDistance = distance;
            }
        }

        return closestPlayer;
    }

/*    @EventHandler
    public void onPlayerPlace(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final ArenaImpl arena = arenaManager.get(player);

        if (arena == null || arena.getMatch() == null) {
            return;
        }

        if (isEnabled(arena, Characteristic.PLACE)) {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getItem() != null) {
                ItemStack item = event.getItem();
                Block clickedBlock = event.getClickedBlock();

                if (clickedBlock == null) {
                    return;
                }

                Location location = clickedBlock.getRelative(event.getBlockFace()).getLocation();
                EntityType entityType = getEntityType(item.getType());

                if (entityType != null) {
                    Entity entity = location.getWorld().spawnEntity(location, entityType);
                    arena.getMatch().placedEntities.add(entity);
                }
            }
        }else {
            event.setCancelled(true);
        }
    }

    private EntityType getEntityType(Material material) {
        switch (material) {
            case END_CRYSTAL:
                return EntityType.ENDER_CRYSTAL;
            case MINECART:
                return EntityType.MINECART;
            case TNT_MINECART:
                return EntityType.MINECART_TNT;
            case ARMOR_STAND:
                return EntityType.ARMOR_STAND;
            case CHEST_MINECART:
                return EntityType.MINECART_CHEST;
            case HOPPER_MINECART:
                return EntityType.MINECART_HOPPER;
            case FURNACE_MINECART:
                return EntityType.MINECART_FURNACE;
            // Add more placeable entities as needed
            default:
                return null; // Unsupported type
        }
    }*/

    @EventHandler
    public void on(final PlayerMoveEvent event) {
        final Player player = event.getPlayer();
        final ArenaImpl arena = arenaManager.get(player);

        if (player.isDead() || arena == null || !isEnabled(arena, Characteristic.SUMO) || arena.isEndGame()) {
            return;
        }

        final Location to = event.getTo(), from = event.getFrom();

        if ((from.getBlockX() !=
                to.getBlockX() || from.getBlockY() != to.getBlockY() || from.getBlockZ() != to.getBlockZ())
                && arena.getCountdown() != null) {
            from.setPitch(player.getLocation().getPitch());
            from.setYaw(player.getLocation().getYaw());
            event.setTo(from);
            return;
        }

        final Block block = event.getFrom().getBlock();

        if (!(block.getType().name().contains("WATER") || block.getType().name().contains("LAVA"))) {
            return;
        }

        player.setHealth(0);
    }

    @EventHandler
    public void on(final PlayerInteractEvent event) {
        if (!event.hasItem() || !event.getAction().name().contains("RIGHT")) {
            return;
        }

        final Player player = event.getPlayer();
        final ArenaImpl arena = arenaManager.get(player);

        if (arena == null || !isEnabled(arena, Characteristic.SOUP)) {
            return;
        }

        final ItemStack item = event.getItem();

        if (item == null || item.getType() != Items.MUSHROOM_SOUP) {
            return;
        }

        event.setUseItemInHand(Result.DENY);

        if (config.isSoupCancelIfAlreadyFull() && player.getHealth() == PlayerUtil.getMaxHealth(player)) {
            return;
        }

        final ItemStack bowl = config.isSoupRemoveEmptyBowl() ? null : new ItemStack(Material.BOWL);

        if (CompatUtil.isPre1_10()) {
            player.getInventory().setItem(player.getInventory().getHeldItemSlot(), bowl);
        } else {
            if (event.getHand() == EquipmentSlot.OFF_HAND) {
                player.getInventory().setItemInOffHand(bowl);
            } else {
                player.getInventory().setItemInMainHand(bowl);
            }
        }

        final double regen = config.getSoupHeartsToRegen() * 2.0;
        final double oldHealth = player.getHealth();
        final double maxHealth = PlayerUtil.getMaxHealth(player);
        player.setHealth(Math.min(oldHealth + regen, maxHealth));
    }

    @EventHandler(ignoreCancelled = true)
    public void on(final EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player player) || !(event.getRegainReason() == RegainReason.SATIATED || event.getRegainReason() == RegainReason.REGEN)) {
            return;
        }

        final ArenaImpl arena = arenaManager.get(player);

        if (arena == null || !isEnabled(arena, Characteristic.UHC)) {
            return;
        }

        event.setCancelled(true);
    }

    private class ComboPre1_14Listener implements Listener {

        @EventHandler
        public void on(final MatchStartEvent event) {
            final ArenaImpl arena = arenaManager.get(event.getMatch().getArena().getName());

            if (arena == null || !isEnabled(arena, Characteristic.COMBO)) {
                return;
            }

            for (final Player player : event.getPlayers()) {
                MetadataUtil.put(plugin, player, METADATA_KEY, player.getMaximumNoDamageTicks());
                player.setMaximumNoDamageTicks(0);
            }
        }

        @EventHandler
        public void on(final MatchEndEvent event) {
            final ArenaImpl arena = arenaManager.get(event.getMatch().getArena().getName());

            if (arena == null || !isEnabled(arena, Characteristic.COMBO)) {
                return;
            }

            final DuelMatch match = arena.getMatch();

            if (match == null) {
                return;
            }

            match.getAllPlayers().forEach(player -> {
                final Object value = MetadataUtil.removeAndGet(plugin, player, METADATA_KEY);

                if (value == null) {
                    return;
                }

                player.setMaximumNoDamageTicks((Integer) value);
            });
        }
    }

    private class ComboPost1_14Listener implements Listener {

        @EventHandler
        public void on(final EntityDamageByEntityEvent event) {
            if (!(event.getEntity() instanceof Player player)) {
                return;
            }

            final ArenaImpl arena = arenaManager.get(player);

            if (arena == null || !isEnabled(arena, Characteristic.COMBO)) {
                return;
            }

            boolean isCritical = !player.isOnGround() &&
                    !player.isSneaking() &&
                    player.getFallDistance() > 0;
            if (isCritical) {
                // Cancel the extra critical damage by setting it to normal attack damage
                event.setDamage(event.getDamage() / 1.5); // Critical hits are 150%, reverse it
            }

            plugin.doSyncAfter(() -> player.setNoDamageTicks(0), 1);
        }
    }
}