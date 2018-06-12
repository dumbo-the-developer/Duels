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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.Getter;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.api.event.user.UserCreateEvent;
import me.realized.duels.api.user.User;
import me.realized.duels.config.Config;
import me.realized.duels.config.Lang;
import me.realized.duels.util.DateUtil;
import me.realized.duels.util.Loadable;
import me.realized.duels.util.Log;
import me.realized.duels.util.compat.Players;
import me.realized.duels.util.profile.ProfileUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class UserManager implements Loadable, Listener, me.realized.duels.api.user.UserManager {

    private final DuelsPlugin plugin;
    private final Config config;
    private final Lang lang;
    private final File folder;
    private final Map<UUID, UserData> users = new ConcurrentHashMap<>();

    private volatile int defaultRating;
    private volatile int maxDisplayMatches;

    @Getter
    private volatile boolean loaded;
    @Getter
    private volatile List<SortedEntry<String, Integer>> topWins;
    private volatile long winsLastUpdate;
    @Getter
    private volatile List<SortedEntry<String, Integer>> topLosses;
    private volatile long lossesLastUpdate;

    public UserManager(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.lang = plugin.getLang();
        this.folder = new File(plugin.getDataFolder(), "users");

        if (!folder.exists()) {
            folder.mkdir();
        }

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void handleLoad() {
        this.defaultRating = config.getDefaultRating();
        this.maxDisplayMatches = 10;

        plugin.doAsync(() -> {
            final File[] files = folder.listFiles();

            if (files != null && files.length > 0) {
                for (final File file : files) {
                    final String fileName = file.getName();

                    if (!fileName.endsWith(".json")) {
                        continue;
                    }

                    final String name = fileName.substring(0, fileName.length() - 5);

                    if (!ProfileUtil.isUUID(name)) {
                        continue;
                    }

                    final UUID uuid = UUID.fromString(name);

                    if (users.containsKey(uuid)) {
                        continue;
                    }

                    try (Reader reader = new InputStreamReader(new FileInputStream(file))) {
                        final UserData user = plugin.getGson().fromJson(reader, UserData.class);
                        user.defaultRating = defaultRating;
                        user.maxDisplayMatches = maxDisplayMatches;
                        // Player might have logged in while reading the file
                        users.putIfAbsent(uuid, user);
                    } catch (IOException ex) {
                        Log.error("Failed to load data of " + uuid + ": " + ex.getMessage());
                    }
                }
            }

            loaded = true;
        });

        plugin.doAsyncRepeat(() -> {
            if (!loaded) {
                return;
            }

            List<SortedEntry<String, Integer>> result = sorted(User::getWins);
            topWins = Collections.unmodifiableList(result.size() > 10 ? result.subList(0, 10) : result);
            winsLastUpdate = System.currentTimeMillis();
            result = sorted(User::getLosses);
            topLosses = Collections.unmodifiableList(result.size() > 10 ? result.subList(0, 10) : result);
            lossesLastUpdate = System.currentTimeMillis();
        }, 20L * 5, 20L * 60);
    }

    @Override
    public void handleUnload() {
        loaded = false;
        saveUsers(Players.getOnlinePlayers());
        users.clear();
    }

    public String getNextWinsUpdate() {
        return DateUtil.format((winsLastUpdate + 1000L * 60L - System.currentTimeMillis()) / 1000L);
    }

    public String getNextLossesUpdate() {
        return DateUtil.format((lossesLastUpdate + 1000L * 60L - System.currentTimeMillis()) / 1000L);
    }

    @Nullable
    @Override
    public UserData get(@Nonnull final UUID uuid) {
        return users.get(uuid);
    }

    @Nullable
    @Override
    public UserData get(@Nonnull final Player player) {
        return get(player.getUniqueId());
    }

    @Override
    public <V extends Comparable<V>> List<SortedEntry<String, V>> sorted(@Nonnull final Function<User, V> function) {
        return users.values().stream()
            .map(data -> new SortedEntry<>(data.getName(), function.apply(data)))
            .sorted(Comparator.reverseOrder())
            .collect(Collectors.toList());
    }

    private void saveUsers(final Collection<? extends Player> players) {
        for (final Player player : players) {
            final UserData user = users.remove(player.getUniqueId());

            if (user != null) {
                trySave(player, user);
            }
        }
    }

    private UserData tryLoad(final Player player) {
        final File file = new File(folder, player.getUniqueId() + ".json");

        if (!file.exists()) {
            final UserData user = new UserData(player);
            user.defaultRating = defaultRating;
            user.maxDisplayMatches = maxDisplayMatches;
            plugin.doSync(() -> plugin.getServer().getPluginManager().callEvent(new UserCreateEvent(user)));
            return user;
        }

        try (Reader reader = new InputStreamReader(new FileInputStream(file))) {
            final UserData user = plugin.getGson().fromJson(reader, UserData.class);

            if (!player.getName().equals(user.getName())) {
                user.setName(player.getName());
            }

            return user;
        } catch (IOException ex) {
            ex.printStackTrace();
            Log.error("An error occured while loading userdata of " + player.getName() + ": " + ex.getMessage());
            return null;
        }
    }

    private void trySave(final Player player, final UserData data) {
        final File file = new File(folder, player.getUniqueId() + ".json");

        try {
            if (!file.exists()) {
                file.createNewFile();
            }

            try (Writer writer = new OutputStreamWriter(new FileOutputStream(file))) {
                plugin.getGson().toJson(data, writer);
                writer.flush();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            Log.error("An error occured while saving userdata of " + player.getName() + ": " + ex.getMessage());
        }
    }

    @EventHandler
    public void on(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final UserData user = users.get(player.getUniqueId());

        if (user != null) {
            if (!player.getName().equals(user.getName())) {
                user.setName(player.getName());
            }

            return;
        }

        plugin.doAsync(() -> {
            final UserData data = tryLoad(player);

            if (data == null) {
                lang.sendMessage(event.getPlayer(), "ERROR.data-load-failure");
                return;
            }

            users.put(event.getPlayer().getUniqueId(), data);
        });
    }

    @EventHandler
    public void on(final PlayerQuitEvent event) {
        final UserData user = users.remove(event.getPlayer().getUniqueId());

        if (user != null) {
            plugin.doAsync(() -> trySave(event.getPlayer(), user));
        }
    }
}
