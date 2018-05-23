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

package me.realized.duels.spectate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import me.realized._duels.utilities.inventory.ItemBuilder;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.arena.Arena;
import me.realized.duels.arena.ArenaManager;
import me.realized.duels.cache.PlayerData;
import me.realized.duels.cache.PlayerDataCache;
import me.realized.duels.config.Config;
import me.realized.duels.config.Lang;
import me.realized.duels.util.Loadable;
import me.realized.duels.util.PlayerUtil;
import me.realized.duels.util.compat.Collisions;
import me.realized.duels.util.compat.CompatUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

public class SpectateManager implements Loadable, Listener {

    private final Config config;
    private final Lang lang;
    private final ArenaManager arenaManager;
    private final PlayerDataCache playerDataCache;

    private final Map<UUID, Spectator> spectators = new HashMap<>();

    public SpectateManager(final DuelsPlugin plugin) {
        this.config = plugin.getConfiguration();
        this.lang = plugin.getLang();
        this.arenaManager = plugin.getArenaManager();
        this.playerDataCache = plugin.getPlayerDataCache();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void handleLoad() throws Exception {

    }

    @Override
    public void handleUnload() throws Exception {

    }

    public Optional<Spectator> get(final Player player) {
        return Optional.ofNullable(spectators.get(player.getUniqueId()));
    }

    public boolean isSpectating(final Player player) {
        return get(player).isPresent();
    }

    public void startSpectating(final Player player, final Player target, final boolean silent) {
        if (isSpectating(player)) {
            lang.sendMessage(player, "ERROR.already-spectating.sender");
            return;
        }

        if (arenaManager.isInMatch(player)) {
            lang.sendMessage(player, "ERROR.already-in-match.sender");
            return;
        }

        final Optional<Arena> found = arenaManager.get(target);

        if (!found.isPresent()) {
            lang.sendMessage(player, "ERROR.not-in-match", "player", target.getName());
            return;
        }

        if (!playerDataCache.get(player).isPresent()) {
            playerDataCache.put(player);
        }

        PlayerUtil.reset(player);

        final Arena arena = found.get();
        arena.getPlayers().forEach(uuid -> {
            final Player arenaPlayer = Bukkit.getPlayer(uuid);

            if (arenaPlayer != null && arenaPlayer.canSee(player)) {
                arenaPlayer.hidePlayer(player);
            }
        });

        player.teleport(target, PlayerTeleportEvent.TeleportCause.SPECTATE);
        player.getInventory().setItem(8, ItemBuilder.builder().type(Material.PAPER).name("&cStop Spectating").build());
        player.setAllowFlight(true);
        player.setFlying(true);
        Collisions.setCollidable(player, false);
        spectators.put(player.getUniqueId(), new Spectator(player, target, arena));

        if (player.hasPermission("duels.spectate.anonymously") || silent) {
            return;
        }

        lang.sendMessage(arena.getPlayers(), "SPECTATE.arena-broadcast", true, "player", player.getName());
    }

    public void startSpectating(final Player player, final Player target) {
        startSpectating(player, target, false);
    }

    public void stopSpectating(final Player player, final boolean end) {
        final Optional<Spectator> found = get(player);

        if (!found.isPresent()) {
            return;
        }

        spectators.remove(player.getUniqueId());
        PlayerUtil.reset(player);

        final Spectator spectator = found.get();
        player.setFlying(false);
        player.setAllowFlight(false);
        Collisions.setCollidable(player, true);

        final Optional<PlayerData> cached = playerDataCache.remove(player);

        if (cached.isPresent()) {
            final PlayerData playerData = cached.get();
            player.teleport(playerData.getLocation());
            playerData.restore(player);
        } else {
            // teleport to lobby?
        }

        spectator.getArena().getPlayers().forEach(uuid -> {
            final Player arenaPlayer = Bukkit.getPlayer(uuid);

            if (arenaPlayer != null && arenaPlayer.canSee(player)) {
                arenaPlayer.showPlayer(player);
            }
        });

        if (!end) {
            lang.sendMessage(player, "SPECTATE.stop", "player", spectator.getTargetName());
        }
    }

    public Set<UUID> getAllPlayers() {
        return spectators.keySet();
    }


    @EventHandler
    public void on(final PlayerInteractEvent event) {
        if (!event.getAction().name().startsWith("RIGHT")) {
            return;
        }

        final Player player = event.getPlayer();
        final ItemStack held = CompatUtil.isPre1_9() ? player.getItemInHand() : player.getInventory().getItemInMainHand();

        if (held == null || held.getType() != Material.PAPER) {
            return;
        }

        stopSpectating(player, false);
    }


    @EventHandler
    public void on(final PlayerQuitEvent event) {
        stopSpectating(event.getPlayer(), false);
    }


    @EventHandler(ignoreCancelled = true)
    public void on(final PlayerCommandPreprocessEvent event) {
        final Player player = event.getPlayer();

        if (!isSpectating(player)) {
            return;
        }

        final String command = event.getMessage().substring(1).split(" ")[0].toLowerCase();

        if (command.equalsIgnoreCase("spectate") || command.equalsIgnoreCase("spec") || config.getSpecWhitelistedCommands().contains(command)) {
            return;
        }

        event.setCancelled(true);
        lang.sendMessage(player, "SPECTATE.prevent-command", "command", event.getMessage());
    }


    @EventHandler(ignoreCancelled = true)
    public void on(final PlayerTeleportEvent event) {
        final Player player = event.getPlayer();

        if (!isSpectating(player)) {
            return;
        }

        event.setCancelled(true);
        lang.sendMessage(player, "SPECTATE.prevent-teleportation");
    }


    @EventHandler(ignoreCancelled = true)
    public void on(final PlayerDropItemEvent event) {
        final Player player = event.getPlayer();

        if (!isSpectating(player)) {
            return;
        }

        event.setCancelled(true);
        lang.sendMessage(player, "SPECTATE.prevent-item-drop");
    }


    @EventHandler(ignoreCancelled = true)
    public void on(PlayerPickupItemEvent event) {
        if (!isSpectating(event.getPlayer())) {
            return;
        }

        event.setCancelled(true);
    }


    @EventHandler(ignoreCancelled = true)
    public void on(final InventoryClickEvent event) {
        if (!isSpectating((Player) event.getWhoClicked())) {
            return;
        }

        event.setCancelled(true);
    }


    @EventHandler
    public void on(final InventoryDragEvent event) {
        if (!isSpectating((Player) event.getWhoClicked())) {
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

        if (!isSpectating(player)) {
            return;
        }

        event.setCancelled(true);
        lang.sendMessage(player, "SPECTATE.prevent-attack");
    }


    @EventHandler(ignoreCancelled = true)
    public void on(final EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player) || !isSpectating((Player) event.getEntity())) {
            return;
        }

        event.setCancelled(true);
    }
}
