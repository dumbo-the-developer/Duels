package com.meteordevelopments.duels.player;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Charsets;
import lombok.Getter;
import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.arena.ArenaImpl;
import com.meteordevelopments.duels.arena.ArenaManagerImpl;
import com.meteordevelopments.duels.config.Config;
import com.meteordevelopments.duels.data.LocationData;
import com.meteordevelopments.duels.data.PlayerData;
import com.meteordevelopments.duels.hook.hooks.EssentialsHook;
import com.meteordevelopments.duels.match.team.TeamDuelMatch;
import com.meteordevelopments.duels.teleport.Teleport;
import com.meteordevelopments.duels.util.Loadable;
import com.meteordevelopments.duels.util.Log;
import com.meteordevelopments.duels.util.PlayerUtil;
import com.meteordevelopments.duels.util.io.FileUtil;
import com.meteordevelopments.duels.util.json.JsonUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Manages:
 * (1) player info cache for restoration after matches.
 * (2) lobby location for teleportation after matches.
 */
public class PlayerInfoManager implements Loadable {

    private static final String CACHE_FILE_NAME = "player-cache.json";
    private static final String LOBBY_FILE_NAME = "lobby.json"; // Assuming you still want to use JSON for lobby
    private static final String KIT_LOBBY_FILE_NAME = "kit_lobby.json";

    private static final String ERROR_LOBBY_LOAD = "Could not load lobby location!";
    private static final String ERROR_LOBBY_SAVE = "Could not save lobby location!";
    private static final String ERROR_LOBBY_DEFAULT = "Lobby location was not set, using %s's spawn location as default. Use the command /duels setlobby in-game to set the lobby location.";

    private final DuelsPlugin plugin;
    private final Config config;
    private final File cacheFile;
    private final File lobbyFile;
    private final File kitlobbyFile;

    private final Map<UUID, PlayerInfo> cache = new HashMap<>();

    private Teleport teleport;
    private EssentialsHook essentials;
    private ArenaManagerImpl arenaManager;

    @Getter
    private Location lobby;
    @Getter
    private Location kitLobby;

    public PlayerInfoManager(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.cacheFile = new File(plugin.getDataFolder(), CACHE_FILE_NAME);
        this.lobbyFile = new File(plugin.getDataFolder(), LOBBY_FILE_NAME);
        this.kitlobbyFile = new File(plugin.getDataFolder(), KIT_LOBBY_FILE_NAME);
        plugin.doSyncAfter(() -> Bukkit.getPluginManager().registerEvents(new PlayerInfoListener(), plugin), 1L);
    }

    @Override
    public void handleLoad() throws IOException {
        this.teleport = plugin.getTeleport();
        this.essentials = plugin.getHookManager().getHook(EssentialsHook.class);
        this.arenaManager = plugin.getArenaManager();

        if (FileUtil.checkNonEmpty(cacheFile, false)) {
            try (final Reader reader = new InputStreamReader(new FileInputStream(cacheFile), Charsets.UTF_8)) {
                final Map<UUID, PlayerData> data = JsonUtil.getObjectMapper().readValue(reader, new TypeReference<HashMap<UUID, PlayerData>>() {});

                if (data != null) {
                    for (final Map.Entry<UUID, PlayerData> entry : data.entrySet()) {
                        cache.put(entry.getKey(), entry.getValue().toPlayerInfo());
                    }
                }
            }

            cacheFile.delete();
        }

        if (FileUtil.checkNonEmpty(lobbyFile, false)) {
            try (final Reader reader = new InputStreamReader(Files.newInputStream(lobbyFile.toPath()), Charsets.UTF_8)) {
                this.lobby = JsonUtil.getObjectMapper().readValue(reader, LocationData.class).toLocation();
            } catch (IOException ex) {
                Log.error(this, ERROR_LOBBY_LOAD, ex);
            }
        }

        if (FileUtil.checkNonEmpty(kitlobbyFile, false)) {
            try (final Reader reader = new InputStreamReader(Files.newInputStream(kitlobbyFile.toPath()), Charsets.UTF_8)) {
                this.kitLobby = JsonUtil.getObjectMapper().readValue(reader, LocationData.class).toLocation();
            } catch (IOException ex) {
                Log.error(this, ERROR_LOBBY_LOAD, ex);
            }
        }

        // If lobby is not found or invalid, use the default world's spawn location for lobby.
        if (lobby == null || lobby.getWorld() == null) {
            final World world = Bukkit.getWorlds().getFirst();
            this.lobby = world.getSpawnLocation();
            Log.warn(this, String.format(ERROR_LOBBY_DEFAULT, world.getName()));
        }

        if (kitLobby == null || kitLobby.getWorld() == null) {
            final World world = Bukkit.getWorlds().getFirst();
            this.kitLobby = world.getSpawnLocation();
            Log.warn(this, String.format(ERROR_LOBBY_DEFAULT, world.getName()));
        }
    }

    @Override
    public void handleUnload() throws IOException {
        Bukkit.getOnlinePlayers().stream().filter(Player::isDead).forEach(player -> {
            final PlayerInfo info = remove(player);

            if (info != null) {
                player.spigot().respawn();
                // FIXED: Wait for teleport to complete before restoring to prevent dimension change errors
                teleport.tryTeleport(player, info.getLocation(), () -> {
                    PlayerUtil.reset(player);
                    DuelsPlugin.getSchedulerAdapter().runTask(player, () -> {
                        if (player.isOnline()) {
                            info.restore(player);
                        }
                    });
                });
            }
        });

        if (cache.isEmpty()) {
            return;
        }

        final Map<UUID, PlayerData> data = new HashMap<>();

        for (final Map.Entry<UUID, PlayerInfo> entry : cache.entrySet()) {
            data.put(entry.getKey(), PlayerData.fromPlayerInfo(entry.getValue()));
        }

        try (final Writer writer = new OutputStreamWriter(new FileOutputStream(cacheFile), Charsets.UTF_8)) {
            JsonUtil.getObjectWriter().writeValue(writer, data);
            writer.flush();
        }

        cache.clear();
    }

    /**
     * Sets a lobby location at given player's location.
     *
     * @param player Player to get location for lobby
     * @return true if setting lobby was successful, false otherwise
     */
    public boolean setLobby(final Player player) {
        final Location lobby = player.getLocation().clone();

        try (final Writer writer = new OutputStreamWriter(Files.newOutputStream(lobbyFile.toPath()), Charsets.UTF_8)) {
            JsonUtil.getObjectWriter().writeValue(writer, LocationData.fromLocation(lobby));
            writer.flush();
            this.lobby = lobby;
            return true;
        } catch (IOException ex) {
            Log.error(this, ERROR_LOBBY_SAVE, ex);
            return false;
        }
    }

    public boolean setKitLobby(final Player player) {
        final Location lobby = player.getLocation().clone();

        try (final Writer writer = new OutputStreamWriter(Files.newOutputStream(kitlobbyFile.toPath()), Charsets.UTF_8)) {
            JsonUtil.getObjectWriter().writeValue(writer, LocationData.fromLocation(lobby));
            writer.flush();
            this.kitLobby = lobby;
            return true;
        } catch (IOException ex) {
            Log.error(this, ERROR_LOBBY_SAVE, ex);
            return false;
        }
    }

    /**
     * Gets cached PlayerInfo instance for given player.
     *
     * @param player Player to get cached PlayerInfo instance
     * @return cached PlayerInfo instance or null if not found
     */
    public PlayerInfo get(final Player player) {
        return cache.get(player.getUniqueId());
    }

    /**
     * Creates a cached PlayerInfo instance for given player.
     *
     * @param player Player to create a cached PlayerInfo instance
     * @param excludeInventory true to exclude inventory contents from being stored in PlayerInfo, false otherwise
     */
    public void create(final Player player, final boolean excludeInventory) {
        final PlayerInfo info = new PlayerInfo(player, excludeInventory);

        if (!config.isTeleportToLastLocation()) {
            info.setLocation(lobby.clone());
        }

        cache.put(player.getUniqueId(), info);
    }

    /**
     * Creates a cached PlayerInfo instance for given player with explicit experience restore control.
     *
     * @param player Player to create a cached PlayerInfo instance
     * @param excludeInventory true to exclude inventory contents from being stored in PlayerInfo, false otherwise
     * @param restoreExperience true to restore experience/level on return, false to keep any changes made during duel
     */
    public void create(final Player player, final boolean excludeInventory, final boolean restoreExperience) {
        final PlayerInfo info = new PlayerInfo(player, excludeInventory, restoreExperience);

        if (!config.isTeleportToLastLocation()) {
            info.setLocation(lobby.clone());
        }

        cache.put(player.getUniqueId(), info);
    }

    /**
     * Calls {@link #create(Player, boolean)} with excludeInventory defaulting to false.
     *
     * @see {@link #create(Player, boolean)}
     */
    public void create(final Player player) {
        create(player, false);
    }

    /**
     * Removes the given player from cache.
     *
     * @param player Player to remove from cache
     * @return Removed PlayerInfo instance or null if not found
     */
    public PlayerInfo remove(final Player player) {
        return cache.remove(player.getUniqueId());
    }

    private class PlayerInfoListener implements Listener {

        // Handles case of some players causing respawn to skip somehow.
        @EventHandler(priority = EventPriority.HIGHEST)
        public void on(final PlayerJoinEvent event) {
            final Player player = event.getPlayer();

            if (player.isDead()) {
                return;
            }

            final PlayerInfo info = remove(player);

            if (info == null) {
                return;
            }

            // FIXED: Delay teleport and restoration to allow player to fully join in Folia
            // Async teleport during join conflicts with player loading process
            // FIXED: Wait for teleport to complete before restoring to prevent dimension change errors
            DuelsPlugin.getSchedulerAdapter().runTaskLater(player, () -> {
                if (player.isOnline() && !player.isDead()) {
                    teleport.tryTeleport(player, info.getLocation(), () -> {
                        // Restore player state after teleport completes
                        if (player.isOnline() && !player.isDead()) {
                            DuelsPlugin.getSchedulerAdapter().runTask(player, () -> {
                                if (player.isOnline() && !player.isDead()) {
                                    info.restore(player);
                                }
                            });
                        }
                    });
                }
            }, 5L); // 5 tick delay to ensure player is fully loaded
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void on(final PlayerRespawnEvent event) {
            final Player player = event.getPlayer();
            final PlayerInfo info = get(player);

            if (info == null) {
                return;
            }

            // Check if player is in a team match and is dead (spectator)
            final ArenaImpl arena = arenaManager.get(player);
            if (arena != null && arena.getMatch() instanceof TeamDuelMatch teamMatch) {
                if (teamMatch.isDead(player)) {
                    // Player is dead in team match, keep them in arena as spectator
                    // Use the arena's center position for spectating
                    event.setRespawnLocation(Objects.requireNonNull(arena.getPosition(1)));
                    
                    // Schedule to set spectator mode after respawn - use entity-specific scheduler for Folia compatibility
                    plugin.doSyncAfter(() -> {
                        if (player.isOnline() && arenaManager.get(player) == arena) {
                            DuelsPlugin.getSchedulerAdapter().runTask(player, () -> {
                                player.setGameMode(GameMode.SPECTATOR);
                                player.setAllowFlight(true);
                                player.setFlying(true);
                            });
                        }
                    }, 1L);
                    return;
                }
            }

            event.setRespawnLocation(info.getLocation());

            if (essentials != null) {
                essentials.setBackLocation(player, event.getRespawnLocation());
            }

            plugin.doSyncAfter(() -> {
                // Do not remove cached data if player left while respawning.
                if (!player.isOnline()) {
                    return;
                }
                
                // Double-check: Don't remove PlayerInfo if player is dead in team match
                final ArenaImpl stillInArena = arenaManager.get(player);
                if (stillInArena != null && stillInArena.getMatch() instanceof TeamDuelMatch stillTeamMatch) {
                    if (stillTeamMatch.isDead(player)) {
                        // Player is still dead in team match, don't restore them
                        return;
                    }
                }

                final PlayerInfo infoToRestore = remove(player);
                if (infoToRestore != null) {
                    // FIXED: Wait for teleport to complete before restoring to prevent dimension change errors
                    teleport.tryTeleport(player, infoToRestore.getLocation(), () -> {
                        DuelsPlugin.getSchedulerAdapter().runTask(player, () -> {
                            if (player.isOnline()) {
                                infoToRestore.restore(player);
                            }
                        });
                    });
                }
            }, 1L);
        }
    }
}
