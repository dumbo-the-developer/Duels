package com.meteordevelopments.duelsffa.listener;

import com.meteordevelopments.duelsffa.FfaExtension;
import com.meteordevelopments.duelsffa.arena.FfaArena;
import com.meteordevelopments.duelsffa.arena.FfaPlayerManager;
import com.meteordevelopments.duelsffa.arena.LeaveReason;
import com.meteordevelopments.duelsffa.config.FfaConfig;
import com.meteordevelopments.duelsffa.config.Lang;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class FfaListener implements Listener {

    private final FfaExtension extension;
    private final FfaConfig config;
    private final Lang lang;
    private final FfaPlayerManager playerManager;

    public FfaListener(final FfaExtension extension) {
        this.extension = extension;
        this.config = extension.getConfiguration();
        this.lang = extension.getLang();
        this.playerManager = extension.getPlayerManager();
    }

    @EventHandler
    public void on(PlayerQuitEvent event) {
        playerManager.leave(event.getPlayer(), LeaveReason.DISCONNECT);
    }

    @EventHandler
    public void on(PlayerKickEvent event) {
        playerManager.leave(event.getPlayer(), LeaveReason.DISCONNECT);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void on(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (!playerManager.isInFfa(player)) {
            return;
        }
        event.setKeepInventory(true);
        event.getDrops().clear();
        event.setDroppedExp(0);
        event.setKeepLevel(true);
    }

    @EventHandler
    public void on(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        FfaArena arena = playerManager.getArena(player);
        if (arena == null) {
            return;
        }
        Location spawn = arena.getRandomSpawn();
        if (spawn != null) {
            event.setRespawnLocation(spawn);
        }
        extension.getApi().doSyncAfter(() -> playerManager.applyKitAfterRespawn(player), 1L);
    }

    @EventHandler
    public void on(PlayerMoveEvent event) {
        if (!config.isPreventLeaveArena()) {
            return;
        }
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null || (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ())) {
            return;
        }
        Player player = event.getPlayer();
        FfaArena arena = playerManager.getArena(player);
        if (arena == null || arena.getZone() == null) {
            return;
        }
        if (!arena.getZone().contains(to)) {
            playerManager.leave(player, LeaveReason.LEFT_ARENA);
            lang.sendMessage(player, "ERROR.leave-arena");
        }
    }

    @EventHandler
    public void on(PlayerTeleportEvent event) {
        if (!config.isPreventLeaveArena()) {
            return;
        }
        Player player = event.getPlayer();
        FfaArena arena = playerManager.getArena(player);
        if (arena == null || arena.getZone() == null) {
            return;
        }
        Location to = event.getTo();
        if (to != null && !arena.getZone().contains(to)) {
            playerManager.leave(player, LeaveReason.LEFT_ARENA);
            lang.sendMessage(player, "ERROR.leave-arena");
        }
    }
}
