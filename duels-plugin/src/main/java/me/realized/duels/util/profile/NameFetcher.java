package me.realized.duels.util.profile;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public final class NameFetcher {

    private static final ScheduledExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor();
    private static final String MOJANG_URL = "https://sessionserver.mojang.com/session/minecraft/profile/";
    private static final String GAMEAPIS_URL = "https://ss.gameapis.net/name/";
    private static final JSONParser JSON_PARSER = new JSONParser();
    private static final Cache<UUID, String> UUID_TO_NAME = CacheBuilder.newBuilder()
        .concurrencyLevel(4)
        .maximumSize(1000)
        .expireAfterWrite(30, TimeUnit.MINUTES)
        .build();

    private NameFetcher() {}

    static void getNames(final List<UUID> uuids, final Consumer<Map<UUID, String>> consumer) {
        EXECUTOR_SERVICE.schedule(new NameCollectorTask(uuids, consumer), 0L, TimeUnit.MILLISECONDS);
    }

    private static String getName(final UUID uuid, final String url) {
        final String cached = UUID_TO_NAME.getIfPresent(uuid);

        if (cached != null) {
            return cached;
        }

        try {
            final HttpURLConnection connection = (HttpURLConnection) new URL(url + uuid.toString().replace("-", "")).openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.connect();

            try (final InputStream stream = connection.getInputStream()) {
                if (stream.available() == 0) {
                    return null;
                }

                final JSONObject response = (JSONObject) JSON_PARSER.parse(new InputStreamReader(stream));
                final String name = (String) response.get("name");

                if (name != null) {
                    UUID_TO_NAME.put(uuid, name);
                    return name;
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static class NameCollectorTask implements Runnable {

        private final List<Key> keys = new ArrayList<>();
        private final Consumer<Map<UUID, String>> consumer;
        private final Map<UUID, String> names = new HashMap<>();

        NameCollectorTask(final List<UUID> uuids, final Consumer<Map<UUID, String>> consumer) {
            uuids.forEach(uuid -> keys.add(new Key(uuid)));
            this.consumer = consumer;
        }

        @Override
        public void run() {
            if (keys.isEmpty()) {
                consumer.accept(names);
                return;
            }

            final Key first = keys.get(0);

            if (first.attempts > 1) {
                keys.remove(first);
            } else {
                final String name;
                final Player player;

                if ((player = Bukkit.getPlayer(first.uuid)) != null) {
                    name = player.getName();
                } else {
                    name = getName(first.uuid, first.attempts == 0 ? MOJANG_URL : GAMEAPIS_URL);
                }

                first.attempts++;

                if (name != null) {
                    names.put(first.uuid, name);
                    keys.remove(first);
                }
            }

            // Run with delay to not trigger the rate limit
            EXECUTOR_SERVICE.schedule(this, 250L, TimeUnit.MILLISECONDS);
        }

        private class Key {

            private final UUID uuid;
            private int attempts;

            Key(final UUID uuid) {
                this.uuid = uuid;
            }
        }
    }
}