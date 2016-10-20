package me.realized.duels.dueling;

import me.realized.duels.Core;
import me.realized.duels.arena.Arena;
import me.realized.duels.arena.ArenaManager;
import me.realized.duels.configuration.MainConfig;
import me.realized.duels.data.DataManager;
import me.realized.duels.data.MatchData;
import me.realized.duels.data.UserData;
import me.realized.duels.event.MatchEndEvent;
import me.realized.duels.event.MatchStartEvent;
import me.realized.duels.event.RequestSendEvent;
import me.realized.duels.hooks.EssentialsHook;
import me.realized.duels.hooks.McMMOHook;
import me.realized.duels.kits.Kit;
import me.realized.duels.kits.KitManager;
import me.realized.duels.utilities.Helper;
import me.realized.duels.utilities.Metadata;
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

public class DuelManager implements Listener {

    private final Core instance;
    private final MainConfig config;
    private final EssentialsHook essentialsHook;
    private final McMMOHook mcMMOHook;
    private final DataManager dataManager;
    private final KitManager kitManager;
    private final ArenaManager arenaManager;
    private final SpectatorManager spectatorManager;

    public DuelManager(Core instance) {
        this.instance = instance;
        this.config = instance.getConfiguration();
        this.essentialsHook = (EssentialsHook) instance.getHookManager().get("Essentials");
        this.mcMMOHook = (McMMOHook) instance.getHookManager().get("mcMMO");
        this.dataManager = instance.getDataManager();
        this.kitManager = instance.getKitManager();
        this.arenaManager = instance.getArenaManager();
        this.spectatorManager = instance.getSpectatorManager();
        Bukkit.getPluginManager().registerEvents(this, instance);
    }

    public void startMatch(Player player, Player target, Request request) {
        if (config.isPatchesCancelMatchIfMoved() && (target.getLocation().getX() != request.getBase().getX() || target.getLocation().getY() != request.getBase().getY() || target.getLocation().getZ() != request.getBase().getZ())) {
            Helper.pm(player, "Errors.match-failed", true, "{REASON}", target.getName() + " moved after sending request.");
            Helper.pm(target, "Errors.match-failed", true, "{REASON}", target.getName() + " moved after sending request.");
            return;
        }

        final Arena arena;

        if (!config.isDuelingUseOwnInventory() && config.isDuelingAllowArenaSelecting()) {
            arena = arenaManager.getArena(request.getArena());

            if (arena == null || arena.isUsed()) {
                Helper.pm(player, "Errors.arena-no-longer-available", true);
                Helper.pm(target, "Errors.arena-no-longer-available", true);
                return;
            }
        } else {
            arena = arenaManager.getAvailableArena();

            if (arena == null) {
                Helper.pm(player, "Errors.no-available-arena-found", true);
                Helper.pm(target, "Errors.no-available-arena-found", true);
                return;
            }
        }

        final Location pos1 = arena.getPositions().get(1);
        final Location pos2 = arena.getPositions().get(2);
        Helper.refreshChunk(pos1, pos2);

        String arenaName = request.getArena() != null ? request.getArena() : "random";
        arena.setUsed(true);

        player.closeInventory();
        target.closeInventory();
        arena.getCurrentMatch().setData(player, target);

        Helper.updatePosition(player, pos1);

        if (!Helper.canTeleportTo(player, pos1) || !player.teleport(pos1)) {
            Helper.pm(player, "Errors.match-failed", true, "{REASON}", "Failed to teleport " + player.getName() + " to " + Helper.format(pos1));
            Helper.pm(target, "Errors.match-failed", true, "{REASON}", "Failed to teleport " + player.getName() + " to " + Helper.format(pos1));
            arena.setUsed(false);
            return;
        }

        Helper.updatePosition(target, pos2);

        if (!Helper.canTeleportTo(target, pos2) || !target.teleport(pos2)) {
            Helper.pm(player, "Errors.match-failed", true, "{REASON}", "Failed to teleport " + target.getName() + " to " + Helper.format(pos2));
            Helper.pm(target, "Errors.match-failed", true, "{REASON}", "Failed to teleport " + target.getName() + " to " + Helper.format(pos2));
            arena.setUsed(false);
            return;
        }

        essentialsHook.setUnvanished(player);
        essentialsHook.setUnvanished(target);
        mcMMOHook.disableSkills(player);
        mcMMOHook.disableSkills(target);

        if (!config.isDuelingUseOwnInventory()) {
            Kit contents = kitManager.getKit(request.getKit());
            Helper.reset(true, player, target);
            contents.equip(player, target);
        } else {
            request.setKit("none");
        }

        arena.addPlayers(player, target);
        arena.startCountdown();

        Helper.pm(player, "Dueling.on-request-accept.sender", true, "{PLAYER}", target.getName(), "{KIT}", request.getKit(), "{ARENA}", arenaName);
        Helper.pm(target, "Dueling.on-request-accept.receiver", true, "{PLAYER}", player.getName(), "{KIT}", request.getKit(), "{ARENA}", arenaName);

        if (arenaManager.getGUI() != null) {
            arenaManager.getGUI().update(arenaManager.getArenas());
        }

        MatchStartEvent event = new MatchStartEvent(Collections.unmodifiableList(arena.getPlayers()), arena, request.getArena() == null);
        Bukkit.getPluginManager().callEvent(event);
    }

    @EventHandler
    public void on(RequestSendEvent event) {
        if (config.isPatchesCancelMatchIfMoved()) {
            Helper.pm(event.getSender(), "Dueling.do-not-move-warning", true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void on(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        if (player.hasMetadata("respawn")) {
            Respawn data = (Respawn) player.getMetadata("respawn").get(0).value();
            event.setRespawnLocation(data.getRespawnLocation());

            if (!config.isDuelingUseOwnInventory()) {
                Helper.reset(player, false);
                Helper.setInventory(player, data.getInventoryData().getInventoryContents(), data.getInventoryData().getArmorContents());
            }

            player.removeMetadata("respawn", instance);
            essentialsHook.setBackLocation(player, event.getRespawnLocation());
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void on(PlayerDeathEvent event) {
        final Player dead = event.getEntity();

        if (!arenaManager.isInMatch(dead)) {
            return;
        }

        event.setDeathMessage(null);
        dead.setMetadata("lastMatchDeath", new Metadata(instance, System.currentTimeMillis()));
        mcMMOHook.enableSkills(dead);

        if (!config.isDuelingUseOwnInventory()) {
            event.setKeepLevel(true);
            event.setDroppedExp(0);
            event.getDrops().clear();
        }

        final Arena arena = arenaManager.getArena(dead);
        final Arena.Match match = arena.getCurrentMatch();

        arena.removePlayer(dead.getUniqueId());

        Arena.InventoryData deadData = match.getInventories(dead.getUniqueId());
        final Location lobby = dataManager.getLobby() != null ? dataManager.getLobby() : dead.getWorld().getSpawnLocation();

        if (config.isDuelingTeleportToLatestLocation()) {
            dead.setMetadata("respawn", new Metadata(Core.getInstance(), new Respawn(deadData, match.getLocation(dead.getUniqueId()))));
        } else {
            dead.setMetadata("respawn", new Metadata(Core.getInstance(), new Respawn(deadData, lobby)));
        }

        if (!arena.isEmpty()) {
            final Player target = Bukkit.getPlayer(arena.getPlayers().get(0));
            target.setHealth(target.getMaxHealth());
            target.setFireTicks(0);
            Firework firework = (Firework) target.getWorld().spawnEntity(target.getEyeLocation(), EntityType.FIREWORK);
            FireworkMeta meta = firework.getFireworkMeta();
            meta.addEffect(FireworkEffect.builder().withColor(Color.RED).with(FireworkEffect.Type.BALL_LARGE).withTrail().build());
            firework.setFireworkMeta(meta);
            Helper.broadcast("Dueling.on-match-end", "{WINNER}", target.getName(), "{LOSER}", dead.getName(), "{HEALTH}", Math.round(target.getHealth()) * 0.5D);

            Calendar calendar = new GregorianCalendar();
            match.setEndTimeMillis(System.currentTimeMillis());
            match.setFinishingHealth(Math.round(target.getHealth()) * 0.5);
            MatchData matchData = new MatchData(target.getName(), dead.getName(), calendar.getTimeInMillis(), match.getDuration(), match.getFinishingHealth());
            UserData player = dataManager.getUser(dead.getUniqueId(), false);
            UserData opponent = dataManager.getUser(target.getUniqueId(), false);

            if (player != null && opponent != null) {
                player.addMatch(matchData);
                opponent.addMatch(matchData);
                player.edit(UserData.EditType.ADD, UserData.StatsType.LOSSES, 1);
                opponent.edit(UserData.EditType.ADD, UserData.StatsType.WINS, 1);
            }

            int delay = config.getDuelingDelayUntilTeleport();
            final Location last = arena.getCurrentMatch().getLocation(target.getUniqueId());
            final Arena.InventoryData targetData = match.getInventories(target.getUniqueId());

            if (delay > 0) {
                new BukkitRunnable() {

                    @Override
                    public void run() {
                        handleEnd(arena, dead, target, targetData, lobby, last);
                    }
                }.runTaskLater(instance, delay * 20L);
            } else {
                handleEnd(arena, dead, target, targetData, lobby, last);
            }
        }
    }

    private void handleEnd(Arena arena, Player dead, Player target, Arena.InventoryData data, Location lobby, Location last) {
        spectatorManager.handleMatchEnd(arena);

        if (target.isOnline() && !target.isDead()) {
            arena.removePlayer(target.getUniqueId());

            if (!config.isDuelingTeleportToLatestLocation()) {
                target.teleport(lobby);
            } else {
                target.teleport(last);
            }

            essentialsHook.setBackLocation(target, target.getLocation());
            mcMMOHook.enableSkills(target);

            if (!config.isDuelingUseOwnInventory()) {
                Helper.reset(target, true);

                if (!config.isDuelingRequiresClearedInventory()) {
                    Helper.setInventory(target, data.getInventoryContents(), data.getArmorContents());
                }
            }
        }

        if (config.isDuelingMatchEndCommandsEnabled()) {
            for (String command : config.getDuelingMatchEndCommandsCommands()) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("{WINNER}", target.getName()).replace("{LOSER}", dead.getName()));
            }
        }

        MatchEndEvent event = new MatchEndEvent(Collections.unmodifiableList(Arrays.asList(dead.getUniqueId(), target.getUniqueId())), arena);
        Bukkit.getPluginManager().callEvent(event);
        arena.setUsed(false);

        if (arenaManager.getGUI() != null) {
            arenaManager.getGUI().update(arenaManager.getArenas());
        }
    }

    @EventHandler
    public void on(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (!arenaManager.isInMatch(player)) {
            return;
        }

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

                    if (player != null && player.getWorld().equals(base.getWorld()) && player.getLocation().distance(to) <= 5.0) {
                        Helper.pm(base, "Errors.already-in-match.target", true);
                        event.setCancelled(true);
                        return;
                    }
                }
            }

            for (Spectator spectator : spectatorManager.getSpectators()) {
                Player player = Bukkit.getPlayer(spectator.getOwner());

                if (player != null && player.getWorld().equals(base.getWorld()) && player.getLocation().distance(to) <= 5.0) {
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

    class Respawn {

        private final Arena.InventoryData inventoryData;
        private final Location respawnLocation;

        Respawn(Arena.InventoryData inventoryData, Location respawnLocation) {
            this.inventoryData = inventoryData;
            this.respawnLocation = respawnLocation;
        }

        Arena.InventoryData getInventoryData() {
            return inventoryData;
        }

        Location getRespawnLocation() {
            return respawnLocation;
        }
    }
}
