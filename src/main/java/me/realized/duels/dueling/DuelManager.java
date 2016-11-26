package me.realized.duels.dueling;

import me.realized.duels.Core;
import me.realized.duels.arena.Arena;
import me.realized.duels.arena.ArenaManager;
import me.realized.duels.configuration.MainConfig;
import me.realized.duels.data.*;
import me.realized.duels.event.MatchEndEvent;
import me.realized.duels.event.MatchStartEvent;
import me.realized.duels.event.RequestSendEvent;
import me.realized.duels.hooks.EssentialsHook;
import me.realized.duels.hooks.McMMOHook;
import me.realized.duels.kits.Kit;
import me.realized.duels.kits.KitManager;
import me.realized.duels.utilities.Helper;
import me.realized.duels.utilities.Storage;
import me.realized.duels.utilities.location.Teleport;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.logging.Level;

public class DuelManager implements Listener {

    private final Core instance;
    private final MainConfig config;
    private final Teleport teleport;
    private final PlayerManager playerManager;
    private final EssentialsHook essentialsHook;
    private final McMMOHook mcMMOHook;
    private final DataManager dataManager;
    private final KitManager kitManager;
    private final ArenaManager arenaManager;
    private final SpectatorManager spectatorManager;

    public DuelManager(Core instance) {
        this.instance = instance;
        this.config = instance.getConfiguration();
        this.teleport = instance.getTeleport();
        this.playerManager = instance.getPlayerManager();
        this.essentialsHook = (EssentialsHook) instance.getHookManager().get("Essentials");
        this.mcMMOHook = (McMMOHook) instance.getHookManager().get("mcMMO");
        this.dataManager = instance.getDataManager();
        this.kitManager = instance.getKitManager();
        this.arenaManager = instance.getArenaManager();
        this.spectatorManager = instance.getSpectatorManager();
        Bukkit.getPluginManager().registerEvents(this, instance);
    }

    public boolean startMatch(Player player, Player target, Request request) {
        if (config.isPatchesCancelMatchIfMoved() && (target.getLocation().getX() != request.getBase().getX() || target.getLocation().getY() != request.getBase().getY() || target.getLocation().getZ() != request.getBase().getZ())) {
            Helper.pm(player, "Errors.match-failed", true, "{REASON}", target.getName() + " moved after sending request.");
            Helper.pm(target, "Errors.match-failed", true, "{REASON}", target.getName() + " moved after sending request.");
            return false;
        }

        final Arena arena;

        if (!config.isDuelingUseOwnInventory() && config.isDuelingAllowArenaSelecting()) {
            arena = arenaManager.getArena(request.getArena());

            if (arena == null || arena.isUsed()) {
                Helper.pm(player, "Errors.arena-no-longer-available", true);
                Helper.pm(target, "Errors.arena-no-longer-available", true);
                return false;
            }
        } else {
            arena = arenaManager.getAvailableArena();

            if (arena == null) {
                Helper.pm(player, "Errors.no-available-arena-found", true);
                Helper.pm(target, "Errors.no-available-arena-found", true);
                return false;
            }
        }

        final Location pos1 = arena.getPositions().get(1);
        final Location pos2 = arena.getPositions().get(2);

        player.closeInventory();
        target.closeInventory();

        if (!teleport.isAuthorizedFor(player, pos1)) {
            Helper.pm(player, "Errors.match-failed", true, "{REASON}", "Failed to teleport " + player.getName() + " to " + Helper.format(pos1));
            Helper.pm(target, "Errors.match-failed", true, "{REASON}", "Failed to teleport " + player.getName() + " to " + Helper.format(pos1));
            return false;
        }

        if (!teleport.isAuthorizedFor(target, pos2)) {
            Helper.pm(player, "Errors.match-failed", true, "{REASON}", "Failed to teleport " + target.getName() + " to " + Helper.format(pos2));
            Helper.pm(target, "Errors.match-failed", true, "{REASON}", "Failed to teleport " + target.getName() + " to " + Helper.format(pos2));
            return false;
        }

        String arenaName = request.getArena() != null ? request.getArena() : "random";
        arena.setUsed(true);

        playerManager.setData(player);
        playerManager.setData(target);

        teleport.teleportPlayer(player, pos1);
        teleport.teleportPlayer(target, pos2);

        essentialsHook.setUnvanished(player);
        essentialsHook.setUnvanished(target);

        mcMMOHook.disableSkills(player);
        mcMMOHook.disableSkills(target);

        Storage.get(player).set("matchDeath", true);
        Storage.get(target).set("matchDeath", true);

        if (!config.isDuelingUseOwnInventory()) {
            Kit contents = kitManager.getKit(request.getKit());
            Helper.reset(true, player, target);
            contents.equip(player, target);
        } else {
            request.setKit("none");
        }

        arena.getCurrentMatch().setKit(request.getKit());
        arena.addPlayers(player, target);
        arena.startCountdown();

        Helper.pm(player, "Dueling.on-request-accept.sender", true, "{PLAYER}", target.getName(), "{KIT}", request.getKit(), "{ARENA}", arenaName);
        Helper.pm(target, "Dueling.on-request-accept.receiver", true, "{PLAYER}", player.getName(), "{KIT}", request.getKit(), "{ARENA}", arenaName);

        if (arenaManager.getGUI() != null) {
            arenaManager.getGUI().update(arenaManager.getArenas());
        }

        MatchStartEvent event = new MatchStartEvent(Collections.unmodifiableList(arena.getPlayers()), arena, request.getArena() == null);
        Bukkit.getPluginManager().callEvent(event);
        return true;
    }

    public void handleDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.isDead() && handlePlayerRespawn(player)) {
                instance.logToFile(this, "Player " + player.getUniqueId() + " (" + player.getName() + ") was force-respawned with inventoryData due to plugin disable.", Level.INFO);
            }
        }
    }

    private boolean handlePlayerRespawn(Player player) {
        Object value = Storage.get(player).get("respawn");

        if (value != null && value instanceof PlayerData) {
            PlayerData data = (PlayerData) value;
            player.spigot().respawn();
            player.teleport(data.getLocation());
            Helper.reset(player, false);
            data.restore(player, !config.isDuelingUseOwnInventory());
            Storage.get(player).remove("respawn");
            essentialsHook.setBackLocation(player, data.getLocation());
            return true;
        }

        return false;
    }

    @EventHandler
    public void on(RequestSendEvent event) {
        if (config.isPatchesCancelMatchIfMoved()) {
            Helper.pm(event.getSender(), "Dueling.do-not-move-warning", true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void on(PlayerRespawnEvent event) {
        final Player player = event.getPlayer();
        Object value = Storage.get(player).get("respawn");

        if (value != null && value instanceof PlayerData) {
            final PlayerData data = (PlayerData) value;
            event.setRespawnLocation(data.getLocation());
            Storage.get(player).remove("respawn");
            essentialsHook.setBackLocation(player, event.getRespawnLocation());

            new BukkitRunnable() {

                @Override
                public void run() {
                    data.restore(player, !config.isDuelingUseOwnInventory());
                }
            }.runTaskLater(instance, 1L);
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void on(PlayerDeathEvent event) {
        final Player dead = event.getEntity();
        final Arena arena = arenaManager.getArena(dead);

        if (arena == null) {
            return;
        }

        event.setDeathMessage(null);

        if (!config.isDuelingUseOwnInventory()) {
            event.setKeepLevel(true);
            event.setDroppedExp(0);
            event.getDrops().clear();
        }

        // For FactionsHook
        Storage.get(dead).remove("matchDeath");
        mcMMOHook.enableSkills(dead);

        final Arena.Match match = arena.getCurrentMatch();

        arena.removePlayer(dead.getUniqueId());

        // For #handleEnd. Do not handle end if both died.
        match.setDead(dead.getUniqueId(), true);

        // For MatchEndEvent
        match.setEndReason(MatchEndEvent.EndReason.OPPONENT_DEFEAT);

        // NOTE TO SELF: Replaced from private Respawn class
        final PlayerData deadData = playerManager.getData(dead);
        playerManager.removeData(dead);

        // set PlayerData's location to lobby if tp-to-latest is disabled.
        if (!config.isDuelingTeleportToLatestLocation()) {
            deadData.setLocation(dataManager.getLobby());
        }

        essentialsHook.setBackLocation(dead, deadData.getLocation());

        // Add storage to be handled later on by RespawnEvent
        Storage.get(dead).set("respawn", deadData);

        if (!arena.isEmpty()) {
            final Player target = Bukkit.getPlayer(arena.getPlayers().get(0));

            Firework firework = (Firework) target.getWorld().spawnEntity(target.getEyeLocation(), EntityType.FIREWORK);
            FireworkMeta meta = firework.getFireworkMeta();
            meta.addEffect(FireworkEffect.builder().withColor(Color.RED).with(FireworkEffect.Type.BALL_LARGE).withTrail().build());
            firework.setFireworkMeta(meta);

            // Broadcast end
            Helper.broadcast("Dueling.on-match-end", "{WINNER}", target.getName(), "{LOSER}", dead.getName(), "{HEALTH}", Math.round(target.getHealth()) * 0.5D, "{KIT}", match.getKit());

            // Generate MatchData
            Calendar calendar = new GregorianCalendar();
            MatchData matchData = new MatchData(target.getName(), dead.getName(), calendar.getTimeInMillis(), (int) match.getDuration(), Math.round(target.getHealth()) * 0.5);
            UserData player = dataManager.getUser(dead.getUniqueId(), false);
            UserData opponent = dataManager.getUser(target.getUniqueId(), false);

            // Add if both are not null
            if (player != null && opponent != null) {
                player.addMatch(matchData);
                opponent.addMatch(matchData);
                player.edit(UserData.EditType.ADD, UserData.StatsType.LOSSES, 1);
                opponent.edit(UserData.EditType.ADD, UserData.StatsType.WINS, 1);
            }

            final int delay = config.getDuelingDelayUntilTeleport();
            final PlayerData winnerData = playerManager.getData(target);
            playerManager.removeData(target);

            if (!config.isDuelingTeleportToLatestLocation()) {
                winnerData.setLocation(dataManager.getLobby());
            }

            if (delay > 0) {
                new BukkitRunnable() {

                    @Override
                    public void run() {
                        handleEnd(arena, match, dead, target, winnerData);
                    }
                }.runTaskLater(instance, delay * 20L);
            } else {
                handleEnd(arena, match, dead, target, winnerData);
            }
        }
    }

    private void handleEnd(Arena arena, Arena.Match match, Player dead, Player target, PlayerData data) {
        spectatorManager.handleMatchEnd(arena);

        if (target.isOnline() && !match.wasDead(target.getUniqueId())) {
            arena.removePlayer(target.getUniqueId());

            Location to = data.getLocation();

            if (!teleport.isAuthorizedFor(target, to)) {
                target.setHealth(0.0D);
                Helper.pm(target, "&4Teleportation failed! You were killed to prevent staying in the arena.", false);
            } else {
                teleport.teleportPlayer(target, to);
            }

            essentialsHook.setBackLocation(target, to);
            mcMMOHook.enableSkills(target);

            // Since this player is confirmed alive, no need to handle faction power loss stuff
            Storage.get(dead).remove("matchDeath");

            if (!config.isDuelingUseOwnInventory()) {
                Helper.reset(target, true);
                data.restore(target, !config.isDuelingRequiresClearedInventory());
            }
        }

        if (config.isDuelingMatchEndCommandsEnabled()) {
            for (String command : config.getDuelingMatchEndCommandsCommands()) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("{WINNER}", target.getName()).replace("{LOSER}", dead.getName()));
            }
        }

        // MatchEndEvent handling
        MatchEndEvent event = new MatchEndEvent(Collections.unmodifiableList(Arrays.asList(dead.getUniqueId(), target.getUniqueId())), arena, match.getEndReason());
        Bukkit.getPluginManager().callEvent(event);

        // Finally, after everything, the arena is back to available state!
        arena.setUsed(false);

        // TODO: 11/22/16 Improve GUI system. Creating new inventories every update just... doesn't seem so efficient.
        if (arenaManager.getGUI() != null) {
            arenaManager.getGUI().update(arenaManager.getArenas());
        }
    }

    @EventHandler
    public void on(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Arena arena = arenaManager.getArena(player);

        if (arena == null) {
            return;
        }

        arena.getCurrentMatch().setEndReason(MatchEndEvent.EndReason.OPPONENT_QUIT);
        player.setHealth(0.0D);
    }

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void on(PlayerTeleportEvent event) {
        Player base = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();

        if (!config.isPatchesStrictTeleportation() && from.getWorld().equals(to.getWorld()) && from.distance(to) < 1.0) {
            return;
        }

        PlayerTeleportEvent.TeleportCause cause = event.getCause();

        if (cause == PlayerTeleportEvent.TeleportCause.COMMAND && config.isPatchesCancelTeleportToPlayersInMatch() && !base.isOp() && !base.hasPermission("duels.admin") && !arenaManager.isInMatch(base)) {
            for (Arena arena : arenaManager.getArenas()) {
                for (UUID uuid : arena.getPlayers()) {
                    Player player = Bukkit.getPlayer(uuid);

                    if (player != null && player.getWorld().equals(to.getWorld()) && player.getLocation().distance(to) <= 5.0) {
                        Helper.pm(base, "Errors.already-in-match.target", true);
                        event.setCancelled(true);
                        return;
                    }
                }
            }

            for (Spectator spectator : spectatorManager.getSpectators()) {
                Player player = Bukkit.getPlayer(spectator.getOwner());

                if (player != null && player.getWorld().equals(to.getWorld()) && player.getLocation().distance(to) <= 5.0) {
                    Helper.pm(base, "Errors.is-in-spectator-mode.target", true);
                    event.setCancelled(true);
                    return;
                }
            }
        }

        if (cause == PlayerTeleportEvent.TeleportCause.ENDER_PEARL || !arenaManager.isInMatch(base)) {
            return;
        }

        event.setCancelled(true);
        Helper.pm(base, "Errors.blocked-while-in-match.teleport", true);
    }

    @EventHandler
    public void on(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        if (config.isDuelingDropItem() || !arenaManager.isInMatch(player)) {
            return;
        }

        event.setCancelled(true);
        Helper.pm(player, "Errors.blocked-while-in-match.drop-item", true);
    }

    @EventHandler
    public void on(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();

        if (config.isDuelingPickUpItem() || !arenaManager.isInMatch(player)) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void on(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage().substring(1).split(" ")[0].toLowerCase();

        if (arenaManager.isInMatch(player) && ((config.isDuelingBlockAllCommands() && !config.getDuelingWhitelistedCommands().contains(command)) || (!config.isDuelingBlockAllCommands() && config.getDuelingDisabledCommands().contains(command)))) {
            event.setCancelled(true);
            Helper.pm(player, "Errors.blocked-while-in-match.command", true);
        }
    }

    @EventHandler
    public void on(PlayerToggleFlightEvent event) {
        if (event.isFlying() && arenaManager.isInMatch(event.getPlayer())) {
            event.setCancelled(true);
            event.getPlayer().setAllowFlight(false);
            Helper.pm(event.getPlayer(), "Errors.blocked-while-in-match.fly", true);
        }
    }

    @EventHandler
    public void on(InventoryOpenEvent event) {
        if (config.isPatchesFixInventoryOpen() && arenaManager.isInMatch((Player) event.getPlayer())) {
            event.setCancelled(true);
            Helper.pm(event.getPlayer(), "Errors.blocked-while-in-match.open-inventory", true);
        }
    }
}
