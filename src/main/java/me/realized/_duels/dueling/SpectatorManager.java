package me.realized._duels.dueling;

import me.realized._duels.Core;
import me.realized._duels.arena.Arena;
import me.realized._duels.arena.ArenaManager;
import me.realized._duels.configuration.MainConfig;
import me.realized._duels.data.PlayerData;
import me.realized._duels.data.PlayerManager;
import me.realized._duels.hooks.EssentialsHook;
import me.realized._duels.utilities.Helper;
import me.realized._duels.utilities.inventory.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class SpectatorManager implements Listener {
    
    private final MainConfig config;
    private final EssentialsHook hook;
    private final PlayerManager playerManager;
    private final ArenaManager arenaManager;

    private List<Spectator> spectators = new ArrayList<>();

    public SpectatorManager(Core instance) {
        this.config = instance.getConfiguration();
        this.hook = (EssentialsHook) instance.getHookManager().get("Essentials");
        this.playerManager = instance.getPlayerManager();
        this.arenaManager = instance.getArenaManager();
        Bukkit.getPluginManager().registerEvents(this, instance);
    }

    public boolean spectate(Player base, Player target) {
        if (isSpectating(base)) {
            Helper.pm(base, "Errors.already-spectating", true);
            return false;
        }

        if (arenaManager.isInMatch(base)) {
            Helper.pm(base, "Errors.already-in-match.sender", true);
            return false;
        }

        Arena arena = arenaManager.getArena(target);

        if (arena == null) {
            Helper.pm(base, "Errors.players-in-match-only", true);
            return false;
        }

        playerManager.setData(base);
        Helper.reset(base, true);
        Spectator spectator = new Spectator(base, target.getName(), arena);

        // Hide the base player from the players in match
        for (UUID uuid : arena.getCurrentMatch().getMatchStarters()) {
            Player player = Bukkit.getPlayer(uuid);

            if (player == null) {
                continue;
            }

            player.hidePlayer(base);
        }

        base.getInventory().setItem(8, ItemBuilder.builder().type(Material.REDSTONE).name("&cStop Spectating").build());
        // Teleport before adding to spectators to prevent tp getting cancelled
        base.teleport(target, PlayerTeleportEvent.TeleportCause.SPECTATE);
        base.setAllowFlight(true);
        base.setFlying(true);

        // Finally add to spectators
        spectators.add(spectator);
        Helper.pm(base, "Spectating.started-spectating", true, "{PLAYER}", target.getName());

        if (!base.hasPermission("duels.spectate.anonymously")) {
            Helper.broadcast(arena, "Spectating.arena-broadcast", true, "{PLAYER}", base.getName());
        }

        return true;
    }

    private Spectator getByPlayer(Player player) {
        for (Spectator spectator : spectators) {
            if (spectator.getOwner().equals(player.getUniqueId())) {
                return spectator;
            }
        }

        return null;
    }

    public boolean isSpectating(Player player) {
        return getByPlayer(player) != null;
    }

    public void stopSpectating(Player base) {
        stopSpectating(base, getByPlayer(base), false);
    }

    private void stopSpectating(Player base, Spectator spectator, boolean matchEnd) {
        spectators.remove(spectator);
        base.teleport(spectator.getBase());
        hook.setBackLocation(base, spectator.getBase());
        Helper.reset(base, false);
        base.setFlying(false);
        base.setAllowFlight(false);

        PlayerData data = playerManager.getData(base);

        if (data != null) {
            data.restore(base, !config.isSpectatingRequiresClearedInventory());
            playerManager.removeData(base);
        }

        for (UUID uuid : spectator.getTarget().getCurrentMatch().getMatchStarters()) {
            Player player = Bukkit.getPlayer(uuid);

            if (player == null) {
                continue;
            }

            player.showPlayer(base);
        }

        if (!matchEnd) {
            Helper.pm(base, "Spectating.stopped-spectating", true, "{PLAYER}", spectator.getSpectatingName());
        } else {
            Helper.pm(base, "Spectating.spectating-match-ended", true);
        }
    }

    public void handleMatchEnd(Arena arena) {
        Iterator<Spectator> iterator = spectators.iterator();

        while (iterator.hasNext()) {
            Spectator spectator = iterator.next();

            if (!spectator.getTarget().equals(arena)) {
                continue;
            }

            iterator.remove();

            Player base = Bukkit.getPlayer(spectator.getOwner());

            // Just in case
            if (base == null) {
                continue;
            }

            stopSpectating(base, spectator, true);
        }
    }

    public List<Spectator> getSpectators() {
        return spectators;
    }

    // Cancel any command except whitelisted commands while in spectator mode.
    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void on(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        if (!isSpectating(player)) {
            return;
        }

        String command = event.getMessage().substring(1).split(" ")[0].toLowerCase();

        if (command.isEmpty() || command.equalsIgnoreCase("spectate") || command.equalsIgnoreCase("spec") || config.getSpectatingWhitelistedCommands().contains(command)) {
            return;
        }

        event.setCancelled(true);
        Helper.pm(player, "Errors.cannot-use-while-spectating", true);
    }

    // Cancel any type of teleportation in spectator mode.
    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void on(PlayerTeleportEvent event) {
        Player player = event.getPlayer();

        if (!isSpectating(player)) {
            return;
        }

        event.setCancelled(true);
        Helper.pm(player, "Errors.cannot-use-while-spectating", true);
    }

    // Cancel dropping item while spectating.
    @EventHandler (ignoreCancelled = true)
    public void on(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        if (!isSpectating(player)) {
            return;
        }

        event.setCancelled(true);
        Helper.pm(player, "Errors.cannot-use-while-spectating", true);
    }

    // Cancel pickup while spectating.
    @EventHandler (ignoreCancelled = true)
    public void on(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();

        if (!isSpectating(player)) {
            return;
        }

        event.setCancelled(true);
    }

    // Listen to stopSpectating
    @SuppressWarnings("deprecation")
    @EventHandler
    public void on(PlayerInteractEvent event) {
        if (!event.getAction().name().startsWith("RIGHT")) {
            return;
        }

        Player base = event.getPlayer();
        ItemStack held;

        if (Helper.isPre1_9()) {
            held = base.getInventory().getItemInHand();
        } else {
            held = base.getInventory().getItemInMainHand();
        }

        if (held == null || held.getType() != Material.REDSTONE) {
            return;
        }

        Spectator spectator = getByPlayer(base);

        if (spectator == null) {
            return;
        }

        stopSpectating(base, spectator, false);
    }

    // Listen to spectator quitting, set back to starting location
    @EventHandler
    public void on(PlayerQuitEvent event) {
        Player base = event.getPlayer();
        Spectator spectator = getByPlayer(base);

       if (spectator == null) {
            return;
        }

        spectators.remove(spectator);
        Helper.reset(base, false);
        base.setFlying(false);
        base.setAllowFlight(false);
        base.teleport(spectator.getBase());
        hook.setBackLocation(base, spectator.getBase());
    }

    // Listen to any type of inventory interaction
    @EventHandler (ignoreCancelled = true)
    public void on(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        if (!isSpectating(player)) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void on(InventoryDragEvent event) {
        Player player = (Player) event.getWhoClicked();

        if (!isSpectating(player)) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler (ignoreCancelled = true)
    public void on(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player damager = (Player) event.getDamager();

            if (!isSpectating(damager)) {
                return;
            }

            Helper.pm(damager, "Errors.cannot-use-while-spectating", true);
            event.setCancelled(true);
        } else if (event.getDamager() instanceof Projectile && event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            if (!isSpectating(player)) {
                return;
            }

            event.setCancelled(true);
        }
    }
}
