package me.realized.duels.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.util.Loadable;
import me.realized.duels.util.Log;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class UserDataManager implements Loadable, Listener {

    private final DuelsPlugin plugin;
    private final File folder;
    private final Map<UUID, UserData> users = new HashMap<>();

    public UserDataManager(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.folder = new File(plugin.getDataFolder(), "users");

        if (!folder.exists()) {
            folder.mkdir();
        }

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void handleLoad() throws Exception {

    }

    @Override
    public void handleUnload() throws Exception {

    }

    public Optional<UserData> get(final UUID uuid) {
        return Optional.ofNullable(users.get(uuid));
    }

    public Optional<UserData> get(final Player player) {
        return get(player.getUniqueId());
    }

    private void loadUser(final Player player, final Consumer<Optional<UserData>> callback) {
        plugin.doAsync(() -> {
            final File file = new File(folder, player.getUniqueId() + ".json");

            if (!file.exists()) {
                callback.accept(Optional.of(new UserData(player)));
                return;
            }

            try (Reader reader = new InputStreamReader(new FileInputStream(file))) {
                final UserData data = plugin.getGson().fromJson(reader, UserData.class);

                if (!player.getName().equals(data.getName())) {
                    data.setName(player.getName());
                }

                callback.accept(Optional.of(data));
            } catch (IOException ex) {
                callback.accept(Optional.empty());
                ex.printStackTrace();
                Log.error("An error occured while loading userdata of " + player.getName() + ": " + ex.getMessage());
            }
        });
    }

    private void saveUser(final Player player, final UserData data) {
        plugin.doAsync(() -> {
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
        });
    }

    @EventHandler
    public void on(final PlayerJoinEvent event) {
        loadUser(event.getPlayer(), result -> {
            if (!result.isPresent()) {
                // TODO: 4/9/18 Replace this to be called with a method handling configuration messages
                event.getPlayer().sendMessage(ChatColor.RED + "An error occured while loading your data. Please contact an administrator.");
                return;
            }

            plugin.doSync(() -> users.put(event.getPlayer().getUniqueId(), result.get()));
        });
    }

    @EventHandler
    public void on(final PlayerQuitEvent event) {
        final Optional<UserData> cached = Optional.ofNullable(users.remove(event.getPlayer().getUniqueId()));

        if (!cached.isPresent()) {
            return;
        }

        saveUser(event.getPlayer(), cached.get());
    }
}
