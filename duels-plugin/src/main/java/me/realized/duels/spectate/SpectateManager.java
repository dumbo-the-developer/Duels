package me.realized.duels.spectate;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.arena.Arena;
import me.realized.duels.arena.ArenaManager;
import me.realized.duels.arena.Match;
import me.realized.duels.config.Config;
import me.realized.duels.config.Lang;
import me.realized.duels.extra.Permissions;
import me.realized.duels.extra.Teleport;
import me.realized.duels.player.PlayerInfo;
import me.realized.duels.player.PlayerInfoManager;
import me.realized.duels.util.Loadable;
import me.realized.duels.util.PlayerUtil;
import me.realized.duels.util.StringUtil;
import me.realized.duels.util.compat.Collisions;
import me.realized.duels.util.inventory.ItemBuilder;
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

    private final DuelsPlugin plugin;
    private final Config config;
    private final Lang lang;
    private final ArenaManager arenaManager;
    private final PlayerInfoManager playerManager;
    private final Map<Player, Spectator> spectators = new HashMap<>();

    private Teleport teleport;

    public SpectateManager(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.lang = plugin.getLang();
        this.arenaManager = plugin.getArenaManager();
        this.playerManager = plugin.getPlayerManager();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void handleLoad() {
        this.teleport = plugin.getTeleport();
    }

    @Override
    public void handleUnload() {

    }

    public Spectator get(final Player player) {
        return spectators.get(player);
    }

    public boolean isSpectating(final Player player) {
        return get(player) != null;
    }

    public void startSpectating(final Player player, final Player target) {
        if (isSpectating(player)) {
            lang.sendMessage(player, "ERROR.already-spectating.sender");
            return;
        }

        if (arenaManager.isInMatch(player)) {
            lang.sendMessage(player, "ERROR.already-in-match.sender");
            return;
        }

        final Arena arena = arenaManager.get(target);

        if (arena == null) {
            lang.sendMessage(player, "ERROR.not-in-match", "player", target.getName());
            return;
        }

        playerManager.put(player, new PlayerInfo(player, !config.isSpecRequiresClearedInventory()));
        PlayerUtil.reset(player);

        final Match match = arena.getMatch();

        if (match != null) {
            match.getPlayers().forEach(arenaPlayer -> {
                if (arenaPlayer.isOnline() && arenaPlayer.canSee(player)) {
                    arenaPlayer.hidePlayer(player);
                }
            });
        }

        final Spectator spectator = new Spectator(player, target, arena);
        spectators.put(player, spectator);
        teleport.tryTeleport(player, target.getLocation());
        spectator.setTeleported(true);
        player.getInventory().setItem(8, ItemBuilder.of(Material.PAPER).name("&cStop Spectating").build());
        player.setAllowFlight(true);
        player.setFlying(true);
        Collisions.setCollidable(player, false);

        if (player.hasPermission(Permissions.SPEC_ANON)) {
            return;
        }

        arena.getMatch().getPlayers().forEach(matchPlayer -> lang.sendMessage(matchPlayer, "SPECTATE.arena-broadcast", true, "player", player.getName()));
    }

    public void stopSpectating(final Player player, final boolean end) {
        final Spectator spectator = get(player);

        if (spectator == null) {
            return;
        }

        spectators.remove(player);
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
            match.getPlayers().stream().filter(Player::isOnline).forEach(matchPlayer -> matchPlayer.showPlayer(player));
        }

        if (!end) {
            lang.sendMessage(player, "SPECTATE.stop", "player", spectator.getTargetName());
        }
    }

    public Set<Player> getPlayers() {
        return spectators.keySet();
    }

    @EventHandler
    public void on(final PlayerInteractEvent event) {
        if (!event.getAction().name().startsWith("RIGHT")) {
            return;
        }

        final Player player = event.getPlayer();
        final ItemStack held = player.getInventory().getItem(player.getInventory().getHeldItemSlot());

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
        final Spectator spectator = get(player);

        if (spectator == null || !spectator.isTeleported()) {
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
