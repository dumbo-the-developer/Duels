package me.realized.duels.data;

import me.realized.duels.Core;
import me.realized.duels.event.UserCreateEvent;
import me.realized.duels.utilities.Reloadable;
import me.realized.duels.utilities.Storage;
import me.realized.duels.utilities.location.SimpleLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.*;
import java.util.*;

public class DataManager implements Listener, Reloadable {

    private final Core instance;

    private File folder;
    private Map<UUID, UserData> users = new HashMap<>();
    private Location lobby;

    public DataManager(Core instance) {
        this.instance = instance;
        Bukkit.getPluginManager().registerEvents(this, instance);
    }

    public void load() {
        users.clear();
        folder = new File(instance.getDataFolder(), "users");

        boolean generated = folder.mkdir();

        if (generated) {
            instance.info("Generated data folder.");
        }

        File lobby = new File(instance.getDataFolder(), "lobby.json");

        if (lobby.exists()) {
            try (InputStreamReader reader = new InputStreamReader(new FileInputStream(lobby))) {
                this.lobby = instance.getGson().fromJson(reader, SimpleLocation.class).toLocation();
            } catch (IOException ex) {
                instance.warn("Failed to load lobby location! (" + ex.getMessage() + ")");
            }
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            loadUser(player.getUniqueId(), player.getName(), true);
        }
    }

    public void save() {
        for (UUID uuid : users.keySet()) {
            saveUser(uuid, false);
        }
    }

    private UserData loadUser(UUID uuid, String name, boolean generate) {
        File[] files = folder.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.getName().replace(".json", "").equals(uuid.toString())) {
                    try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file))) {
                        UserData user = instance.getGson().fromJson(reader, UserData.class);
                        users.put(uuid, user);
                        return user;
                    } catch (IOException ex) {
                        instance.warn("Failed to load data for " + uuid + "! (" + ex.getMessage() + ")");
                        return null;
                    }
                }
            }
        }

        if (generate) {
            UserData user = new UserData(uuid, name);
            users.put(uuid, user);

            UserCreateEvent event = new UserCreateEvent(user);
            Bukkit.getPluginManager().callEvent(event);
            return user;
        }

        return null;
    }

    private void saveUser(UUID uuid, boolean log) {
        if (users.get(uuid) == null) {
            return;
        }

        UserData user = users.get(uuid);

        try {
            File dataFile = new File(folder, uuid.toString() + ".json");
            boolean generated = dataFile.createNewFile();

            if (log && generated) {
                instance.info("Generated data file for " + uuid + ".");
            }

            Writer writer = new OutputStreamWriter(new FileOutputStream(dataFile));
            instance.getGson().toJson(user, writer);
            writer.flush();
            writer.close();
        } catch (IOException ex) {
            instance.warn("Failed to save data for " + uuid + "! (" + ex.getMessage() + ")");
        }
    }

    public UserData getUser(UUID uuid, boolean force) {
        return users.get(uuid) != null ? users.get(uuid) : (force ? loadUser(uuid, null, false) : null);
    }

    public Map<UUID, UserData> getUsers() {
        return users;
    }

    public void setLobby(Player player) {
        try {
            File dataFile = new File(instance.getDataFolder(), "lobby.json");
            boolean generated = dataFile.createNewFile();

            if (generated) {
                instance.info("Generated 'lobby.json'.");
            }

            Writer writer = new OutputStreamWriter(new FileOutputStream(dataFile));
            instance.getGson().toJson(new SimpleLocation(lobby), writer);
            writer.flush();
            writer.close();

            this.lobby = player.getLocation().clone();
        } catch (IOException ex) {
            instance.warn("Failed to save lobby location! (" + ex.getMessage() + ")");
        }
    }

    public Location getLobby() {
        return lobby != null ? lobby : Bukkit.getWorlds().get(0).getSpawnLocation();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UserData user = loadUser(player.getUniqueId(), player.getName(), true);

        if (user == null) {
            return;
        }

        if (!user.getName().equals(player.getName())) {
            user.setName(player.getName());
        }

        user.refreshMatches();

        if (!user.getMatches().isEmpty()) {
            Iterator<MatchData> iterator = user.getMatches().iterator();
            Calendar now = new GregorianCalendar();

            while (iterator.hasNext()) {
                MatchData match = iterator.next();

                if (now.getTimeInMillis() - match.getTime() > 604800 * 1000L) {
                    iterator.remove();
                }
            }
        }
    }

    public final Object addData(UserData user, String key, Object value) {
        if (user == null) {
            return false;
        }

        return user.addData(key, value);
    }

    public final boolean removeData(UserData user, String key) {
        return user != null && user.removeData(key);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent event) {
        saveUser(event.getPlayer().getUniqueId(), true);
        users.remove(event.getPlayer().getUniqueId());
        Storage.remove(event.getPlayer());
        instance.getPlayerManager().removeData(event.getPlayer());
    }

    @Override
    public void handleReload(ReloadType type) {
        if (type == ReloadType.STRONG) {
            save();
            load();
        }
    }
}
