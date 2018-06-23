package me.realized.duels.data;

import com.google.common.collect.Lists;
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
import me.realized.duels.kit.Kit;
import me.realized.duels.util.DateUtil;
import me.realized.duels.util.Loadable;
import me.realized.duels.util.Log;
import me.realized.duels.util.UUIDUtil;
import me.realized.duels.util.compat.Players;
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
    private volatile int matchesToDisplay;

    @Getter
    private volatile boolean loaded;
    @Getter
    private volatile TopEntry wins;
    @Getter
    private volatile TopEntry losses;
    @Getter
    private final Map<Kit, TopEntry> topRatings = new ConcurrentHashMap<>();

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
        this.matchesToDisplay = config.getMatchesToDisplay();

        if (matchesToDisplay < 0) {
            matchesToDisplay = 0;
        }

        plugin.doAsync(() -> {
            final File[] files = folder.listFiles();

            if (files != null && files.length > 0) {
                for (final File file : files) {
                    final String fileName = file.getName();

                    if (!fileName.endsWith(".json")) {
                        continue;
                    }

                    final String name = fileName.substring(0, fileName.length() - 5);

                    final UUID uuid = UUIDUtil.parseUUID(name);

                    if (uuid == null || users.containsKey(uuid)) {
                        continue;
                    }

                    try (Reader reader = new InputStreamReader(new FileInputStream(file))) {
                        final UserData user = plugin.getGson().fromJson(reader, UserData.class);
                        user.defaultRating = defaultRating;
                        user.matchesToDisplay = matchesToDisplay;
                        user.refreshMatches();
                        // Player might have logged in while reading the file
                        users.putIfAbsent(uuid, user);
                    } catch (IOException ex) {
                        Log.error("Failed to load data of " + uuid + ": " + ex.getMessage());
                    }
                }
            }

            loaded = true;
        });

        plugin.doSyncRepeat(() -> {
            final Collection<Kit> kits = plugin.getKitManager().getKits();

            plugin.doAsync(() -> {
                if (!loaded) {
                    return;
                }

                List<SortedEntry<String, Integer>> result = sorted(User::getWins);
                wins = new TopEntry("Wins", "wins", result);
                result = sorted(User::getLosses);
                losses = new TopEntry("Losses", "losses", result);
                topRatings.keySet().removeIf(kit -> !kits.contains(kit));

                for (final Kit kit : kits) {
                    result = sorted(user -> user.getRating(kit));
                    topRatings.put(kit, new TopEntry(kit.getName(), "rating", result));
                }
            });
        }, 20L * 5, 20L * 60);
    }

    @Override
    public void handleUnload() {
        loaded = false;
        saveUsers(Players.getOnlinePlayers());
        users.clear();
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
    public List<SortedEntry<String, Integer>> getTopWins() {
        return wins != null ? wins.data : null;
    }

    @Override
    public List<SortedEntry<String, Integer>> getTopLosses() {
        return losses != null ? losses.data : null;
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
            user.matchesToDisplay = matchesToDisplay;
            plugin.doSync(() -> plugin.getServer().getPluginManager().callEvent(new UserCreateEvent(user)));
            return user;
        }

        try (Reader reader = new InputStreamReader(new FileInputStream(file))) {
            final UserData user = plugin.getGson().fromJson(reader, UserData.class);
            user.defaultRating = defaultRating;
            user.matchesToDisplay = matchesToDisplay;
            user.refreshMatches();

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
                lang.sendMessage(event.getPlayer(), "ERROR.data.load-failure");
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

    public class TopEntry {

        private final long lastUpdate;
        @Getter
        private final String name, type;
        @Getter
        private final List<SortedEntry<String, Integer>> data;

        TopEntry(final String name, final String type, final List<SortedEntry<String, Integer>> data) {
            this.lastUpdate = System.currentTimeMillis();
            this.name = name;
            this.type = type;
            this.data = Collections.unmodifiableList(Lists.newArrayList(data.size() > 10 ? data.subList(0, 10) : data));
        }

        public String getNextUpdate() {
            return DateUtil.format((lastUpdate + 1000L * 60L - System.currentTimeMillis()) / 1000L);
        }
    }
}
