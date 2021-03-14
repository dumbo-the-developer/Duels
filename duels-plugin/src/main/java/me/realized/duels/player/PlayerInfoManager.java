package me.realized.duels.player;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.config.Config;
import me.realized.duels.data.LocationData;
import me.realized.duels.hook.hooks.EssentialsHook;
import me.realized.duels.util.Loadable;
import me.realized.duels.util.Log;
import me.realized.duels.util.Teleport;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

/**
 * Manages:
 * (1) player info cache for restoration after matches.
 * (2) lobby location for teleportation after matches.
 */
public class PlayerInfoManager implements Loadable {

    private static final String FILE_NAME = "lobby.json";

    private static final String ERROR_LOBBY_LOAD = "Could not load lobby location!";
    private static final String ERROR_LOBBY_SAVE = "Could not save lobby location!";
    private static final String ERROR_LOBBY_DEFAULT = "Lobby location was not set, using %s's spawn location as default. Use the command /duels setlobby in-game to set the lobby location.";

    private final DuelsPlugin plugin;
    private final Config config;
    private final File file;

    private final Map<UUID, PlayerInfo> cache = Maps.newHashMap();

    private Teleport teleport;
    private EssentialsHook essentials;

    @Getter
    private Location lobby;

    public PlayerInfoManager(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.file = new File(plugin.getDataFolder(), FILE_NAME);
        plugin.doSyncAfter(() -> plugin.getServer().getPluginManager().registerEvents(new PlayerInfoListener(), plugin), 1L);
    }

    @Override
    public void handleLoad() {
        this.teleport = plugin.getTeleport();
        this.essentials = plugin.getHookManager().getHook(EssentialsHook.class);

        if (file.exists()) {
            try (Reader reader = new InputStreamReader(new FileInputStream(file), Charsets.UTF_8)) {
                this.lobby = plugin.getGson().fromJson(reader, LocationData.class).toLocation();
            } catch (IOException ex) {
                Log.error(this, ERROR_LOBBY_LOAD, ex);
            }
        }

        // If lobby is not found or invalid, use the default world's spawn location for lobby.
        if (lobby == null || lobby.getWorld() == null) {
            final World world = Bukkit.getWorlds().get(0);
            this.lobby = world.getSpawnLocation();
            Log.warn(this, String.format(ERROR_LOBBY_DEFAULT, world.getName()));
        }
    }

    // TODO: 3/14/21 Store PlayerInfo cache on a separate file or in each userdata file.
    @Override
    public void handleUnload() {}

    /**
     * Sets a lobby location at given player's location.
     *
     * @param player Player to get location for lobby
     * @return true if setting lobby was successful, false otherwise
     */
    public boolean setLobby(final Player player) {
        final Location lobby = player.getLocation().clone();

        try {
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), Charsets.UTF_8)) {
                plugin.getGson().toJson(new LocationData(lobby), writer);
                writer.flush();
            }

            this.lobby = lobby;
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
     * @param cacheInventory true to cache inventory im PlayerInfo, false otherwise
     */
    public void create(final Player player, final boolean cacheInventory) {
        final PlayerInfo info = new PlayerInfo(player, cacheInventory);

        if (!config.isTeleportToLastLocation()) {
            info.setLocation(lobby.clone());
        }

        cache.put(player.getUniqueId(), info);
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

            if (info != null) {
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
                    info.restore(player);
                }, 1L);
            }
        }
    }
}
