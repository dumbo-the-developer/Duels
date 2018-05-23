/*
 * This file is part of Duels, licensed under the MIT License.
 *
 * Copyright (c) Realized
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.realized.duels.duel;

import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.arena.Arena;
import me.realized.duels.arena.ArenaManager;
import me.realized.duels.arena.Match;
import me.realized.duels.cache.PlayerData;
import me.realized.duels.cache.PlayerDataCache;
import me.realized.duels.cache.Setting;
import me.realized.duels.config.Config;
import me.realized.duels.config.Lang;
import me.realized.duels.data.MatchData;
import me.realized.duels.data.UserData;
import me.realized.duels.data.UserDataManager;
import me.realized.duels.hooks.VaultHook;
import me.realized.duels.kit.Kit;
import me.realized.duels.kit.KitManager;
import me.realized.duels.spectate.SpectateManager;
import me.realized.duels.util.Loadable;
import me.realized.duels.util.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;

public class DuelManager implements Loadable, Listener {

    private final DuelsPlugin plugin;
    private final Config config;
    private final Lang lang;
    private final UserDataManager userDataManager;
    private final ArenaManager arenaManager;
    private final KitManager kitManager;
    private final PlayerDataCache playerDataCache;
    private final SpectateManager spectateManager;

    public DuelManager(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.lang = plugin.getLang();
        this.userDataManager = plugin.getUserManager();
        this.arenaManager = plugin.getArenaManager();
        this.kitManager = plugin.getKitManager();
        this.playerDataCache = plugin.getPlayerDataCache();
        this.spectateManager = plugin.getSpectateManager();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void handleLoad() {}

    @Override
    public void handleUnload() {
        Bukkit.getOnlinePlayers().stream().filter(Player::isDead).forEach(this::forceRespawn);
    }

    private void forceRespawn(final Player player) {
        playerDataCache.remove(player).ifPresent(data -> {
            player.spigot().respawn();
            player.teleport(data.getLocation());
            PlayerUtil.reset(player);
            data.restore(player);
        });
    }

    // todo: Make sure to check for everything again before actually starting the match
    public void startMatch(final Player first, final Player second, final Setting setting, final Map<UUID, List<ItemStack>> items) {
        final Arena arena = setting.getArena() != null ? setting.getArena() : arenaManager.randomArena();

        if (arena == null || !arena.isAvailable()) {
            lang.sendMessage(first, "DUEL.arena-in-use");
            lang.sendMessage(second, "DUEL.arena-in-use");

            if (items != null) {
                items.get(first.getUniqueId()).forEach(item -> first.getInventory().addItem(item));
                items.get(second.getUniqueId()).forEach(item -> second.getInventory().addItem(item));
            }

            return;
        }

        final int bet = setting.getBet();
        final Optional<VaultHook> foundVHook = plugin.getHookManager().getHook(VaultHook.class);
        final VaultHook vaultHook;

        if (bet > 0 && foundVHook.isPresent() && (vaultHook = foundVHook.get()).hasEconomy()) {
            if (!vaultHook.getEconomy().has(first, bet) || !vaultHook.getEconomy().has(second, bet)) {
                lang.sendMessage(first, "DUEL.not-enough-money");
                lang.sendMessage(second, "DUEL.not-enough-money");
                return;
            }

            vaultHook.getEconomy().withdrawPlayer(first, bet);
            vaultHook.getEconomy().withdrawPlayer(second, bet);
        }

        final Kit kit = setting.getKit() != null ? setting.getKit() : kitManager.randomKit();
        arena.setUsed(true);
        arena.setMatch(kit, items, setting.getBet());
        handlePlayer(first, arena, kit, arena.getPositions().get(1));
        handlePlayer(second, arena, kit, arena.getPositions().get(2));
    }

    private void handlePlayer(final Player player, final Arena arena, final Kit kit, final Location location) {
        if (player.getAllowFlight()) {
            player.setFlying(false);
            player.setAllowFlight(false);
        }

        player.closeInventory();

        if (!config.isUseOwnInventoryEnabled()) {
            playerDataCache.put(player);
            PlayerUtil.reset(player);

            if (kit != null) {
                kit.equip(player);
            }
        }

        player.teleport(location);
        arena.add(player);
    }


    @EventHandler
    public void on(final PlayerDeathEvent event) {
        final Player player = event.getEntity();
        final Optional<Arena> result = arenaManager.get(player);

        if (!result.isPresent()) {
            return;
        }

        event.setDeathMessage(null);
        event.setKeepInventory(config.isUseOwnInventoryEnabled() && config.isUseOwnInventoryKeepItems());

        if (!config.isUseOwnInventoryEnabled()) {
            event.setKeepLevel(true);
            event.setDroppedExp(0);
            event.getDrops().clear();
        }

        final Arena arena = result.get();
        arena.remove(player);
        plugin.doSyncAfter(() -> {
            if (arena.size() == 0) {
                Bukkit.broadcastMessage("Tie game");
                arena.setUsed(false);
                return;
            }

            final Player winner = Bukkit.getPlayer(arena.getFirst());
            final Match match = arena.getCurrent();
            final long duration = System.currentTimeMillis() - match.getCreation();
            final long time = new GregorianCalendar().getTimeInMillis();
            final double health = Math.ceil(winner.getHealth()) * 0.5;
            final MatchData matchData = new MatchData(winner.getName(), player.getName(), time, duration, health);
            Optional<UserData> cached = userDataManager.get(player);
            cached.ifPresent(user -> {
                user.addLoss();
                user.addMatch(matchData);
            });
            cached = userDataManager.get(winner);
            cached.ifPresent(user -> {
                user.addWin();
                user.addMatch(matchData);
            });

            plugin.doSyncAfter(() -> {
                // check if player is online before adding bet items. If not, add to PlayerData
                // Add money without teleport delay.
                Bukkit.broadcastMessage("Winner = " + winner.getName());
                arena.setUsed(false);
            }, config.getTeleportDelay() * 20L);
        }, 1L);
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void on(final PlayerRespawnEvent event) {
        final Player player = event.getPlayer();
        final Optional<PlayerData> cached = playerDataCache.remove(player);

        if (cached.isPresent()) {
            final PlayerData data = cached.get();
            event.setRespawnLocation(data.getLocation());
//            essentialsHook.setBackLocation(player, event.getRespawnLocation());
            plugin.doSyncAfter(() -> {
                PlayerUtil.reset(player);
                data.restore(player);
            }, 1L);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void on(final EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        final Player player = (Player) event.getEntity();
        final Optional<Arena> result = arenaManager.get(player);

        if (!result.isPresent()) {
            return;
        }

        final Arena arena = result.get();

        if (arena.size() > 1) {
            return;
        }

        event.setCancelled(true);
    }


    @EventHandler(ignoreCancelled = true)
    public void on(final EntityDamageByEntityEvent event) {
        final Player player;

        if (event.getDamager() instanceof Player) {
            player = (Player) event.getDamager();
        } else if (event.getDamager() instanceof Projectile && ((Projectile) event.getDamager()).getShooter() instanceof Player) {
            player = (Player) ((Projectile) event.getDamager()).getShooter();
        } else {
            return;
        }

        final Optional<Arena> result = arenaManager.get(player);

        if (!result.isPresent() || result.get().has(event.getEntity().getUniqueId())) {
            return;
        }

        event.setCancelled(true);
    }


    @EventHandler
    public void on(final PlayerQuitEvent event) {
        final Player player = event.getPlayer();

        if (!arenaManager.isInMatch(player)) {
            return;
        }

        player.setHealth(0);
    }


    @EventHandler(ignoreCancelled = true)
    public void on(final PlayerDropItemEvent event) {
        if (!config.isPreventItemDrop() || !arenaManager.isInMatch(event.getPlayer())) {
            return;
        }

        event.setCancelled(true);
        lang.sendMessage(event.getPlayer(), "DUEL.prevent-item-drop");
    }


    @EventHandler(ignoreCancelled = true)
    public void on(final PlayerPickupItemEvent event) {
        if (!config.isPreventItemPickup() || !arenaManager.isInMatch(event.getPlayer())) {
            return;
        }

        event.setCancelled(true);
        lang.sendMessage(event.getPlayer(), "DUEL.prevent-item-pickup");
    }


    @EventHandler(ignoreCancelled = true)
    public void on(final PlayerCommandPreprocessEvent event) {
        final String command = event.getMessage().substring(1).split(" ")[0].toLowerCase();

        if (!arenaManager.isInMatch(event.getPlayer()) || (config.isBlockAllCommands() ? config.getWhitelistedCommands().contains(command)
            : !config.getBlacklistedCommands().contains(command))) {
            return;
        }

        event.setCancelled(true);
        lang.sendMessage(event.getPlayer(), "DUEL.prevent-command", "command", event.getMessage());
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void on(PlayerTeleportEvent event) {
        final Player player = event.getPlayer();
        final Location to = event.getTo();

        if (event.getCause() != TeleportCause.SPECTATE) {
            final List<UUID> players = arenaManager.getAllPlayers();
            players.addAll(spectateManager.getAllPlayers());

            for (final UUID uuid : players) {
                final Player target = Bukkit.getPlayer(uuid);

                if (target == null || !isSimilar(target.getLocation(), to)) {
                    continue;
                }

                event.setCancelled(true);
                lang.sendMessage(player, "ERROR.patch-prevent-teleportation");
                return;
            }
        }

        if (!config.isLimitTeleportEnabled() || event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL || !arenaManager.isInMatch(player)) {
            return;
        }

        final Location from = event.getFrom();

        if (from.getWorld().equals(to.getWorld()) && from.distance(to) <= config.getDistanceAllowed()) {
            return;
        }

        event.setCancelled(true);
        lang.sendMessage(player, "DUEL.prevent-teleportation");
    }

    private boolean isSimilar(final Location first, final Location second) {
        return Math.abs(first.getX() - second.getX()) + Math.abs(first.getY() - second.getY()) + Math.abs(first.getZ() - second.getZ()) < 5;
    }
}
