package me.realized.duels.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.config.Lang;
import me.realized.duels.util.Loadable;
import me.realized.duels.util.Log;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class UserDataManager implements Loadable, Listener {

    private final DuelsPlugin plugin;
    private final Lang lang;
    private final File folder;
    private final Map<UUID, UserData> users = new HashMap<>();

    public UserDataManager(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.lang = plugin.getLang();
        this.folder = new File(plugin.getDataFolder(), "users");

        if (!folder.exists()) {
            folder.mkdir();
        }

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void handleLoad() {
        loadUsers(Bukkit.getOnlinePlayers());
    }

    @Override
    public void handleUnload() {
        saveUsers(Bukkit.getOnlinePlayers());
    }

    public Optional<UserData> get(final UUID uuid) {
        return Optional.ofNullable(users.get(uuid));
    }

    public Optional<UserData> get(final Player player) {
        return get(player.getUniqueId());
    }

    private void loadUsers(final Collection<? extends Player> players) {
        for (final Player player : players) {
            tryLoad(player).ifPresent(data -> users.put(player.getUniqueId(), data));
        }
    }

    private void saveUsers(final Collection<? extends Player> players) {
        for (final Player player : players) {
            Optional.ofNullable(users.remove(player.getUniqueId())).ifPresent(data -> trySave(player, data));
        }
    }

    private Optional<UserData> tryLoad(final Player player) {
        final File file = new File(folder, player.getUniqueId() + ".json");

        if (!file.exists()) {
            return Optional.of(new UserData(player));
        }

        try (Reader reader = new InputStreamReader(new FileInputStream(file))) {
            final UserData data = plugin.getGson().fromJson(reader, UserData.class);

            if (!player.getName().equals(data.getName())) {
                data.setName(player.getName());
            }

            return Optional.of(data);
        } catch (IOException ex) {
            ex.printStackTrace();
            Log.error("An error occured while loading userdata of " + player.getName() + ": " + ex.getMessage());
        }

        return Optional.empty();
    }

    private void trySave(final Player player, final UserData data) {
        final File file = new File(folder, player.getUniqueId() + ".json");

        if (!file.exists()) {
            try {
                file.createNewFile();

                try (Writer writer = new OutputStreamWriter(new FileOutputStream(file))) {
                    plugin.getGson().toJson(data, writer);
                    writer.flush();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                Log.error("An error occured while saving userdata of " + player.getName() + ": " + ex.getMessage());
            }
        }
    }

    private void loadUser(final Player player, final Consumer<Optional<UserData>> callback) {
        plugin.doAsync(() -> callback.accept(tryLoad(player)));
    }

    @EventHandler
    public void on(final PlayerJoinEvent event) {
        loadUser(event.getPlayer(), result -> {
            if (!result.isPresent()) {
                lang.sendMessage(event.getPlayer(), "ERROR.data-load-failure");
                return;
            }

            plugin.doSync(() -> users.put(event.getPlayer().getUniqueId(), result.get()));
        });
    }

    @EventHandler
    public void on(final PlayerQuitEvent event) {
        Optional.ofNullable(users.remove(event.getPlayer().getUniqueId())).ifPresent(data -> plugin.doAsync(() -> trySave(event.getPlayer(), data)));
    }
}
