package com.meteordevelopments.duels.redis;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.util.Log;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.util.Objects;
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
        this.host = getEnvOrDefault("REDIS_HOST", "localhost");
        this.port = Integer.parseInt(getEnvOrDefault("REDIS_PORT", "6379"));
        this.password = System.getenv("REDIS_PASSWORD");
        this.database = Integer.parseInt(getEnvOrDefault("REDIS_DB", "0"));

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
        try (UnifiedJedis j = client()) {
            j.publish(channel, message);
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

    private String getEnvOrDefault(final String key, final String def) {
        final String value = System.getenv(key);
        return value != null && !value.isEmpty() ? value : def;
    }
}

