package com.meteordevelopments.duels.config;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.util.config.AbstractConfiguration;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;

public class DatabaseConfig extends AbstractConfiguration<DuelsPlugin> {

    @Getter private String mongoUri;
    @Getter private String mongoDatabase;

    @Getter private String redisHost;
    @Getter private int redisPort;
    @Getter private String redisPassword;
    @Getter private int redisDb;
    @Getter private String serverId;

    public DatabaseConfig(final DuelsPlugin plugin) {
        super(plugin, "DB");
    }

    @Override
    protected void loadValues(FileConfiguration configuration) {
        mongoUri = configuration.getString("mongo.uri", "mongodb://localhost:27017");
        mongoDatabase = configuration.getString("mongo.database", "duels");

        redisHost = configuration.getString("redis.host", "localhost");
        redisPort = configuration.getInt("redis.port", 6379);
        redisPassword = configuration.getString("redis.password", null);
        redisDb = configuration.getInt("redis.db", 0);
        serverId = configuration.getString("serverId", "");
    }
}

