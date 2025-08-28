package com.meteordevelopments.duels.redis;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.util.Log;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.params.SetParams;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RedisService {

    public static final String CHANNEL_INVALIDATE_USER = "duels:invalidate:user";
    public static final String CHANNEL_INVALIDATE_KIT = "duels:invalidate:kit";
    public static final String CHANNEL_INVALIDATE_ARENA = "duels:invalidate:arena";

    private final DuelsPlugin plugin;
    private UnifiedJedis client;
    private ExecutorService subscriberExecutor;
    private String host;
    private int port;
    private String password;
    private int database;

    public RedisService(@NotNull final DuelsPlugin plugin) {
        this.plugin = plugin;
    }

    public void connect() {
        this.host = plugin.getDatabaseConfig().getRedisHost();
        this.port = plugin.getDatabaseConfig().getRedisPort();
        this.password = plugin.getDatabaseConfig().getRedisPassword();
        this.database = plugin.getDatabaseConfig().getRedisDb();

        final var clientConfig = DefaultJedisClientConfig.builder()
                .password(password != null && !password.isEmpty() ? password : null)
                .database(database)
                .build();
        client = new UnifiedJedis(new HostAndPort(this.host, this.port), clientConfig);

        subscriberExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "duels-redis-subscriber");
            t.setDaemon(true);
            return t;
        });

        DuelsPlugin.sendMessage("&aConnected to Redis at &f" + host + ":" + port + " &a(db=" + database + ")");
    }

    public void close() {
        if (client != null) {
            client.close();
        }
        if (subscriberExecutor != null) {
            subscriberExecutor.shutdownNow();
        }
    }

    public UnifiedJedis client() { return client; }

    public void publish(final String channel, final String message) {
        try {
            // Prefix with serverId to allow receivers to ignore self-originated events
            final String serverId = plugin.getDatabaseConfig() != null && plugin.getDatabaseConfig().getServerId() != null && !plugin.getDatabaseConfig().getServerId().trim().isEmpty()
                    ? plugin.getDatabaseConfig().getServerId().trim()
                    : String.valueOf(plugin.getServer().getPort());
            client.publish(channel, serverId + ":" + message);
        } catch (Exception ex) {
            Log.error("Redis publish failed on channel: " + channel, ex);
        }
    }

    public void subscribe(final redis.clients.jedis.JedisPubSub listener, final String... channels) {
        subscriberExecutor.submit(() -> {
            try (Jedis j = new Jedis(host, port)) {
                if (password != null && !password.isEmpty()) { j.auth(password); }
                if (database > 0) { j.select(database); }
                j.subscribe(listener, channels);
            } catch (Exception ex) {
                Log.error("Redis subscribe error", ex);
            }
        });
    }

    private String getEnvOrDefault(final String key, final String def) { return def; }

    public boolean tryAcquireLock(final String key, final int ttlSeconds, final String value) {
        try {
            final String result = client.set(key, value, SetParams.setParams().nx().ex(ttlSeconds));
            return "OK".equalsIgnoreCase(result);
        } catch (Exception ex) {
            Log.error("Redis lock failed: " + key, ex);
            return false;
        }
    }

    public void releaseLock(final String key, final String value) {
        final String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        try {
            client.eval(script, java.util.Collections.singletonList(key), java.util.Collections.singletonList(value));
        } catch (Exception ex) {
            Log.error("Redis unlock failed: " + key, ex);
        }
    }
}

