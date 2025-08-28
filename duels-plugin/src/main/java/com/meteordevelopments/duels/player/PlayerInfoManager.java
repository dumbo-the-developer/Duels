package com.meteordevelopments.duels.player;

import com.google.common.base.Charsets;
import lombok.Getter;
import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.config.Config;
import com.meteordevelopments.duels.data.LocationData;
import com.meteordevelopments.duels.data.PlayerData;
import com.meteordevelopments.duels.hook.hooks.EssentialsHook;
import com.meteordevelopments.duels.teleport.Teleport;
import com.meteordevelopments.duels.util.Loadable;
import com.meteordevelopments.duels.util.Log;
import com.meteordevelopments.duels.util.PlayerUtil;
import com.meteordevelopments.duels.util.io.FileUtil;
import com.meteordevelopments.duels.util.json.JsonUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages:
 * (1) player info cache for restoration after matches.
 * (2) lobby location for teleportation after matches.
 */
public class PlayerInfoManager implements Loadable {

    private static final String CACHE_FILE_NAME = "player-cache.yml"; // Changed to YAML
    private static final String LOBBY_FILE_NAME = "lobby.json"; // Assuming you still want to use JSON for lobby

    private static final String ERROR_LOBBY_LOAD = "Could not load lobby location!";
    private static final String ERROR_LOBBY_SAVE = "Could not save lobby location!";
    private static final String ERROR_LOBBY_DEFAULT = "Lobby location was not set, using %s's spawn location as default. Use the command /duels setlobby in-game to set the lobby location.";

    private final DuelsPlugin plugin;
    private final Config config;
    private final File cacheFile;
    private final File lobbyFile;

    private final Map<UUID, PlayerInfo> cache = new HashMap<>();

    private Teleport teleport;
    private EssentialsHook essentials;

    @Getter
    private Location lobby;

    public PlayerInfoManager(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.cacheFile = new File(plugin.getDataFolder(), CACHE_FILE_NAME);
        this.lobbyFile = new File(plugin.getDataFolder(), LOBBY_FILE_NAME);
        plugin.doSyncAfter(() -> Bukkit.getPluginManager().registerEvents(new PlayerInfoListener(), plugin), 1L);
    }

    @Override
    public void handleLoad() throws IOException {
        this.teleport = plugin.getTeleport();
        this.essentials = plugin.getHookManager().getHook(EssentialsHook.class);

        // Load the player cache from YAML
        if (FileUtil.checkNonEmpty(cacheFile, false)) {
            try (final Reader reader = new InputStreamReader(Files.newInputStream(cacheFile.toPath()), Charsets.UTF_8)) {
                Yaml yaml = new Yaml();
                Map<UUID, PlayerData> data = yaml.load(reader); // Cast the loaded data to Map<UUID, PlayerData>

                if (data != null) {
                    for (final Map.Entry<UUID, PlayerData> entry : data.entrySet()) {
                        cache.put(entry.getKey(), entry.getValue().toPlayerInfo());
                    }
                }
            }

            cacheFile.delete();
        }

        try {
            final var mongo = plugin.getMongoService();
            if (mongo != null) {
                final var doc = mongo.collection("meta").find(new org.bson.Document("_id", "lobby")).first();
                if (doc != null) {
                    final String json = doc.toJson();
                    this.lobby = JsonUtil.getObjectMapper().readValue(json, LocationData.class).toLocation();
                }
            }
        } catch (Exception ex) {
            Log.error(this, ERROR_LOBBY_LOAD, ex);
        }

        // If lobby is not found or invalid, use the default world's spawn location for lobby.
        if (lobby == null || lobby.getWorld() == null) {
            final World world = Bukkit.getWorlds().get(0);
            this.lobby = world.getSpawnLocation();
            Log.warn(this, String.format(ERROR_LOBBY_DEFAULT, world.getName()));
        }
    }

    @Override
    public void handleUnload() throws IOException {
        Bukkit.getOnlinePlayers().stream().filter(Player::isDead).forEach(player -> {
            final PlayerInfo info = remove(player);

            if (info != null) {
                player.spigot().respawn();
                teleport.tryTeleport(player, info.getLocation());
                PlayerUtil.reset(player);
                info.restore(player);
            }
        });

        if (cache.isEmpty()) {
            return;
        }

        // Prepare data for YAML output
        Map<UUID, PlayerData> data = new HashMap<>();

        for (final Map.Entry<UUID, PlayerInfo> entry : cache.entrySet()) {
            data.put(entry.getKey(), PlayerData.fromPlayerInfo(entry.getValue()));
        }

        // Write the player cache to YAML
        try (final Writer writer = new OutputStreamWriter(Files.newOutputStream(cacheFile.toPath()), Charsets.UTF_8)) {
            Yaml yaml = new Yaml();
            yaml.dump(data, writer);
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

        try {
            final var mongo = plugin.getMongoService();
            if (mongo != null) {
                final var collection = mongo.collection("meta");
                final String json = JsonUtil.getObjectWriter().writeValueAsString(LocationData.fromLocation(lobby));
                final org.bson.Document doc = org.bson.Document.parse(json);
                doc.put("_id", "lobby");
                collection.replaceOne(new org.bson.Document("_id", "lobby"), doc, new com.mongodb.client.model.ReplaceOptions().upsert(true));
                this.lobby = lobby;
                return true;
            }
        } catch (Exception ex) {
            Log.error(this, ERROR_LOBBY_SAVE, ex);
        }
        return false;
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

            teleport.tryTeleport(player, info.getLocation());
            info.restore(player);
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void on(final PlayerRespawnEvent event) {
            final Player player = event.getPlayer();
            final PlayerInfo info = get(player);

            if (info == null) {
                return;
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

                remove(player);
                DuelsPlugin.getMorePaperLib().scheduling().entitySpecificScheduler(player).run(() -> info.restore(player), null);
            }, 1L);
        }
    }
}
