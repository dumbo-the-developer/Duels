package me.realized.duels.dueling;

import me.realized.duels.Core;
import me.realized.duels.arena.Arena;
import me.realized.duels.arena.ArenaManager;
import me.realized.duels.configuration.MainConfig;
import me.realized.duels.hooks.EssentialsHook;
import me.realized.duels.utilities.Helper;
import me.realized.duels.utilities.inventory.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
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
    private final ArenaManager manager;

    private List<Spectator> spectators = new ArrayList<>();

    public SpectatorManager(Core instance) {
        this.config = instance.getConfiguration();
        this.hook = (EssentialsHook) instance.getHookManager().get("Essentials");
        this.manager = instance.getArenaManager();
        Bukkit.getPluginManager().registerEvents(this, instance);
    }

    public void spectate(Player base, Player target) {
        if (isSpectating(base)) {
            Helper.pm(base, "Errors.already-spectating", true);
            return;
        }

        if (manager.isInMatch(base)) {
            Helper.pm(base, "Errors.already-in-match.sender", true);
            return;
        }

        Arena arena = manager.getArena(target);

        if (arena == null) {
            Helper.pm(base, "Errors.players-in-match-only", true);
            return;
        }

        Spectator spectator = new Spectator(base, target.getName(), arena);

        // Hide the base player from the players in match
        for (UUID uuid : arena.getPlayers()) {
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

        if (base.hasPermission("duels.spectate.anonymously")) {
            return;
        }

        Helper.broadcast(arena, "Spectating.arena-broadcast", true, "{PLAYER}", base.getName());
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
        stopSpectating(base, getByPlayer(base));
    }

    private void stopSpectating(Player base, Spectator spectator) {
        spectators.remove(spectator);
        base.teleport(spectator.getBase());
        Helper.reset(base, false);
        base.setFlying(false);
        base.setAllowFlight(false);

        for (UUID uuid : spectator.getTarget().getPlayers()) {
            Player player = Bukkit.getPlayer(uuid);

            if (player == null) {
                continue;
            }

            player.showPlayer(base);
        }

        Helper.pm(base, "Spectating.stopped-spectating", true, "{PLAYER}", spectator.getSpectatingName());
    }

    public void handleMatchEnd(Arena arena) {
        Iterator<Spectator> iterator = spectators.iterator();

        while (iterator.hasNext()) {
            Spectator spectator = iterator.next();

            if (!spectator.getTarget().getName().equals(arena.getName())) {
                continue;
            }

            Player base = Bukkit.getPlayer(spectator.getOwner());
            iterator.remove();

            // Just in case
            if (base == null) {
                continue;
            }

            Helper.reset(base, false);
            base.setFlying(false);
            base.setAllowFlight(false);
            base.teleport(spectator.getBase());
            hook.setBackLocation(base, spectator.getBase());

            for (UUID uuid : arena.getPlayers()) {
                Player player = Bukkit.getPlayer(uuid);

                if (player == null) {
                    continue;
                }

                player.showPlayer(base);
            }

            Helper.pm(base, "Spectating.spectating-match-ended", true);
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

        stopSpectating(base, spectator);
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

    // Cancel any type of damaging.
    @EventHandler (ignoreCancelled = true)
    public void on(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        Player damager = (Player) event.getDamager();

        if (!isSpectating(damager)) {
            return;
        }

        event.setCancelled(true);
        Helper.pm(damager, "Errors.cannot-use-while-spectating", true);
    }
}
