package com.meteordevelopments.duelsffa.arena;

import com.meteordevelopments.duels.api.Duels;
import com.meteordevelopments.duels.api.kit.Kit;
import com.meteordevelopments.duelsffa.FfaExtension;
import com.meteordevelopments.duelsffa.config.FfaConfig;
import com.meteordevelopments.duelsffa.util.InventorySnapshot;
import com.meteordevelopments.duelsffa.util.LocationUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FfaPlayerManager {

    public static class JoinResult {
        private final boolean success;
        private final String messageKey;
        private final Object[] placeholders;

        private JoinResult(boolean success, String messageKey, Object[] placeholders) {
            this.success = success;
            this.messageKey = messageKey;
            this.placeholders = placeholders;
        }

        public static JoinResult ok() {
            return new JoinResult(true, null, new Object[0]);
        }

        public static JoinResult error(String key, Object... placeholders) {
            return new JoinResult(false, key, placeholders);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessageKey() {
            return messageKey;
        }

        public Object[] getPlaceholders() {
            return placeholders;
        }
    }

    private final FfaExtension extension;
    private final Duels api;
    private final FfaConfig config;
    private final Map<UUID, FfaSession> sessions = new ConcurrentHashMap<>();

    public FfaPlayerManager(final FfaExtension extension) {
        this.extension = extension;
        this.api = extension.getApi();
        this.config = extension.getConfiguration();
    }

    public JoinResult tryJoin(final Player player, final FfaArena arena) {
        if (arena == null) {
            return JoinResult.error("ERROR.arena-not-found", "name", "?");
        }
        if (sessions.containsKey(player.getUniqueId())) {
            return JoinResult.error("ERROR.already-in-ffa");
        }
        if (!arena.isEnabled()) {
            return JoinResult.error("ERROR.arena-disabled", "name", arena.getName());
        }
        if (arena.isResetting()) {
            return JoinResult.error("ERROR.arena-resetting", "name", arena.getName());
        }
        if (arena.getZone() == null) {
            return JoinResult.error("ERROR.no-zone", "name", arena.getName());
        }
        if (arena.getSpawns().isEmpty()) {
            return JoinResult.error("ERROR.no-spawns", "name", arena.getName());
        }

        final String kitName = arena.getKitName();
        final boolean noKit = arena.isNoKit();
        final InventorySnapshot snapshot = InventorySnapshot.capture(player);
        Kit kit = null;
        if (!noKit) {
            kit = resolveKit(kitName);
            if (kit == null || kit.isRemoved()) {
                return JoinResult.error("ERROR.kit-not-found", "name", kitName);
            }
            if (kit.isUsePermission()) {
                String perm = "duels.kit." + kit.getName().replace(" ", "-").toLowerCase();
                if (!player.hasPermission("duels.kits.*") && !player.hasPermission(perm)) {
                    return JoinResult.error("ERROR.kit-no-permission", "name", kitName);
                }
            }
        }

        Location returnLocation = player.getLocation();
        if (!noKit) {
            player.getInventory().clear();
            player.getInventory().setArmorContents(new org.bukkit.inventory.ItemStack[4]);
            player.getInventory().setItemInOffHand(null);
            kit.equip(player);
        }

        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setSaturation(20.0f);
        player.setFallDistance(0.0f);

        Location spawn = arena.getRandomSpawn();
        if (spawn != null) {
            player.teleport(spawn);
        }

        FfaSession session = new FfaSession(player.getUniqueId(), arena.getName(), kitName, noKit, snapshot, returnLocation);
        sessions.put(player.getUniqueId(), session);
        return JoinResult.ok();
    }

    public FfaArena leave(final Player player, final LeaveReason reason) {
        FfaSession session = sessions.remove(player.getUniqueId());
        if (session == null) {
            return null;
        }

        if (session.getSnapshot() != null) {
            session.getSnapshot().apply(player);
        }

        if (reason.shouldTeleport()) {
            Location lobby = LocationUtil.deserialize(config.getLobby());
            Location target = null;
            if (reason == LeaveReason.REGEN) {
                target = lobby != null ? lobby : player.getWorld().getSpawnLocation();
            } else {
                if (lobby != null) {
                    target = lobby;
                } else if (session.getReturnLocation() != null) {
                    target = session.getReturnLocation();
                } else {
                    target = player.getWorld().getSpawnLocation();
                }
            }
            player.teleport(target);
        }

        return extension.getArenaManager().getArena(session.getArenaName());
    }

    public boolean isInFfa(final Player player) {
        return sessions.containsKey(player.getUniqueId());
    }

    public FfaArena getArena(final Player player) {
        FfaSession session = sessions.get(player.getUniqueId());
        if (session == null) {
            return null;
        }
        return extension.getArenaManager().getArena(session.getArenaName());
    }

    public FfaSession getSession(final Player player) {
        return sessions.get(player.getUniqueId());
    }

    public List<Player> getPlayersInArena(final FfaArena arena) {
        if (arena == null) return Collections.emptyList();
        List<Player> players = new ArrayList<>();
        for (FfaSession session : sessions.values()) {
            if (!arena.getName().equalsIgnoreCase(session.getArenaName())) {
                continue;
            }
            Player player = Bukkit.getPlayer(session.getPlayerId());
            if (player != null) {
                players.add(player);
            }
        }
        return players;
    }

    public void applyKitAfterRespawn(final Player player) {
        FfaSession session = sessions.get(player.getUniqueId());
        if (session == null || session.isNoKit()) {
            return;
        }
        Kit kit = resolveKit(session.getKitName());
        if (kit == null || kit.isRemoved()) {
            return;
        }
        player.getInventory().clear();
        player.getInventory().setArmorContents(new org.bukkit.inventory.ItemStack[4]);
        player.getInventory().setItemInOffHand(null);
        kit.equip(player);
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setSaturation(20.0f);
    }

    public void shutdown() {
        for (UUID uuid : sessions.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                FfaSession session = sessions.get(uuid);
                if (session != null && session.getSnapshot() != null) {
                    session.getSnapshot().apply(player);
                }
            }
        }
        sessions.clear();
    }

    public Kit resolveKit(final String kitName) {
        if (kitName == null) return null;
        Kit kit = api.getKitManager().get(kitName);
        if (kit != null) {
            return kit;
        }
        for (Kit entry : api.getKitManager().getKits()) {
            if (entry.getName().equalsIgnoreCase(kitName)) {
                return entry;
            }
        }
        return null;
    }
}
