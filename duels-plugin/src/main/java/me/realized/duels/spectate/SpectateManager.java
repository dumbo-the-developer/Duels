package me.realized.duels.spectate;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.Permissions;
import me.realized.duels.arena.Arena;
import me.realized.duels.arena.ArenaManager;
import me.realized.duels.arena.Match;
import me.realized.duels.config.Config;
import me.realized.duels.config.Lang;
import me.realized.duels.hook.hooks.MyPetHook;
import me.realized.duels.player.PlayerInfo;
import me.realized.duels.player.PlayerInfoManager;
import me.realized.duels.util.Loadable;
import me.realized.duels.util.PlayerUtil;
import me.realized.duels.util.StringUtil;
import me.realized.duels.util.Teleport;
import me.realized.duels.util.compat.Collisions;
import me.realized.duels.util.compat.Players;
import me.realized.duels.util.inventory.InventoryUtil;
import me.realized.duels.util.inventory.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
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

public class SpectateManager implements Loadable {

    private final DuelsPlugin plugin;
    private final Config config;
    private final Lang lang;
    private final ArenaManager arenaManager;
    private final PlayerInfoManager playerManager;
    private final Map<UUID, Spectator> spectators = new HashMap<>();

    private Teleport teleport;
    private MyPetHook myPet;

    public SpectateManager(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.lang = plugin.getLang();
        this.arenaManager = plugin.getArenaManager();
        this.playerManager = plugin.getPlayerManager();
        plugin.getServer().getPluginManager().registerEvents(new SpectateListener(), plugin);
    }

    @Override
    public void handleLoad() {
        this.teleport = plugin.getTeleport();
        this.myPet = plugin.getHookManager().getHook(MyPetHook.class);
    }

    @Override
    public void handleUnload() {
        spectators.clear();
    }

    public Spectator get(final Player player) {
        return spectators.get(player.getUniqueId());
    }

    public boolean isSpectating(final Player player) {
        return get(player) != null;
    }

    public void startSpectating(final Player player, final Player target) {
        if (isSpectating(player)) {
            lang.sendMessage(player, "ERROR.spectate.already-spectating.sender");
            return;
        }

        if (plugin.getQueueManager().isInQueue(player)) {
            lang.sendMessage(player, "ERROR.duel.already-in-queue");
            return;
        }

        if (arenaManager.isInMatch(player)) {
            lang.sendMessage(player, "ERROR.duel.already-in-match.sender");
            return;
        }

        final Arena arena = arenaManager.get(target);

        if (arena == null) {
            lang.sendMessage(player, "ERROR.spectate.not-in-match", "name", target.getName());
            return;
        }

        playerManager.put(player, new PlayerInfo(player, !config.isSpecRequiresClearedInventory()));
        PlayerUtil.reset(player);

        final Match match = arena.getMatch();

        if (match != null) {
            match.getAllPlayers().forEach(arenaPlayer -> {
                if (arenaPlayer.isOnline() && arenaPlayer.canSee(player)) {
                    arenaPlayer.hidePlayer(player);
                }
            });
        }

        if (myPet != null) {
            myPet.removePet(player);
        }

        final Spectator spectator = new Spectator(player, target, arena);
        spectators.put(player.getUniqueId(), spectator);
        teleport.tryTeleport(player, target.getLocation().clone().add(0, 2, 0));
        spectator.setTeleported(true);
        player.getInventory().setItem(8, ItemBuilder.of(Material.PAPER).name(lang.getMessage("SPECTATE.stop-item.name")).build());
        player.setAllowFlight(true);
        player.setFlying(true);
        Collisions.setCollidable(player, false);
        lang.sendMessage(player, "COMMAND.spectate.start-spectate", "name", target.getName());

        if (player.hasPermission(Permissions.SPEC_ANON)) {
            return;
        }

        arena.getMatch().getAllPlayers().forEach(matchPlayer -> lang.sendMessage(matchPlayer, "SPECTATE.arena-broadcast", "name", player.getName()));
    }

    public void stopSpectating(final Player player, final Spectator spectator, final boolean end) {
        if (!end) {
            spectators.remove(player.getUniqueId());
        }

        PlayerUtil.reset(player);
        player.setFlying(false);
        player.setAllowFlight(false);
        Collisions.setCollidable(player, true);

        final PlayerInfo info = playerManager.removeAndGet(player);

        if (info != null) {
            teleport.tryTeleport(player, info.getLocation(), failed -> {
                failed.setHealth(0);
                failed.sendMessage(StringUtil.color("&cTeleportation failed! You were killed to prevent staying in the arena."));
            });
            info.restore(player);
        } else {
            teleport.tryTeleport(player, playerManager.getLobby());
        }

        final Match match = spectator.getArena().getMatch();

        if (match != null) {
            match.getAllPlayers().stream().filter(Player::isOnline).forEach(matchPlayer -> matchPlayer.showPlayer(player));
        }

        if (!end) {
            lang.sendMessage(player, "COMMAND.spectate.stop-spectate", "name", spectator.getTargetName());
        } else {
            lang.sendMessage(player, "SPECTATE.match-end");
        }
    }

    public void stopSpectating(final Player player, final boolean end) {
        stopSpectating(player, get(player), end);
    }

    public void stopSpectating(final Arena arena) {
        spectators.entrySet().removeIf(entry -> {
            final Spectator spectator = entry.getValue();

            if (!spectator.getArena().equals(arena)) {
                return false;
            }

            stopSpectating(Bukkit.getPlayer(entry.getKey()), spectator, true);
            return true;
        });
    }

    public Collection<Player> getPlayers() {
        return Players.getOnlinePlayers().stream().filter(this::isSpectating).collect(Collectors.toList());
    }

    public Collection<Player> getSpectators(final Arena arena) {
        return spectators.entrySet()
            .stream().filter(entry -> entry.getValue().getArena().equals(arena)).map(spectator -> Bukkit.getPlayer(spectator.getKey())).collect(Collectors.toSet());
    }

    private class SpectateListener implements Listener {

        @EventHandler
        public void on(final PlayerInteractEvent event) {
            final Player player = event.getPlayer();
            final Spectator spectator = get(player);

            if (spectator == null) {
                return;
            }

            if (config.isSpecPreventBlockInteract()) {
                event.setCancelled(true);
            }

            if (event.getAction() == Action.PHYSICAL) {
                return;
            }

            final ItemStack held = InventoryUtil.getItemInHand(player);

            if (held == null || held.getType() != Material.PAPER) {
                return;
            }

            stopSpectating(player, spectator, false);
        }

        @EventHandler
        public void on(final PlayerQuitEvent event) {
            final Player player = event.getPlayer();
            final Spectator spectator = get(player);

            if (spectator == null) {
                return;
            }

            stopSpectating(player, spectator, false);
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
            lang.sendMessage(player, "SPECTATE.prevent.command", "command", event.getMessage());
        }

        @EventHandler(ignoreCancelled = true)
        public void on(final PlayerTeleportEvent event) {
            final Player player = event.getPlayer();
            final Spectator spectator = get(player);

            if (spectator == null || !spectator.isTeleported()) {
                return;
            }

            event.setCancelled(true);
            lang.sendMessage(player, "SPECTATE.prevent.teleportation");
        }

        @EventHandler(ignoreCancelled = true)
        public void on(final PlayerDropItemEvent event) {
            final Player player = event.getPlayer();

            if (!isSpectating(player)) {
                return;
            }

            event.setCancelled(true);
            lang.sendMessage(player, "SPECTATE.prevent.item-drop");
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
            lang.sendMessage(player, "SPECTATE.prevent.pvp");
        }

        @EventHandler(ignoreCancelled = true)
        public void on(final EntityDamageEvent event) {
            if (!(event.getEntity() instanceof Player) || !isSpectating((Player) event.getEntity())) {
                return;
            }

            event.setCancelled(true);
        }
    }
}
