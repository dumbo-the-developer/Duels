package com.meteordevelopments.duels.mongo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.data.UserData;
import com.meteordevelopments.duels.util.Log;
import com.meteordevelopments.duels.util.json.JsonUtil;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Centralized MongoDB access for the plugin. Minimal wrapper to keep other classes simple.
 */
public class MongoService {

    private final DuelsPlugin plugin;
    private MongoClient client;
    private MongoDatabase database;

    public MongoService(@NotNull final DuelsPlugin plugin) {
        this.plugin = plugin;
    }

    public void connect() {
        final String uri = plugin.getDatabaseConfig().getMongoUri();
        final String dbName = plugin.getDatabaseConfig().getMongoDatabase();

        final MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(uri))
                .build();

        client = MongoClients.create(settings);
        database = client.getDatabase(dbName);
        DuelsPlugin.sendMessage("&aConnected to MongoDB database '&f" + dbName + "&a'.");
    }

    public void close() {
        if (client != null) {
            client.close();
        }
    }

    public MongoCollection<Document> collection(@NotNull final String name) {
        return database.getCollection(name);
    }

    private String getEnvOrDefault(final String key, final String def) { return def; }

    // ===== Convenience methods for common data types =====

    public void saveUser(@NotNull final UserData user) {
        try {
            final String json = JsonUtil.getObjectWriter().writeValueAsString(user);
            final Document doc = Document.parse(json);
            doc.put("_id", user.getUuid().toString());
            collection("users").replaceOne(new Document("_id", user.getUuid().toString()), doc, new com.mongodb.client.model.ReplaceOptions().upsert(true));
        } catch (JsonProcessingException ex) {
            Log.error("Failed to serialize user for Mongo save", ex);
        } catch (Exception ex) {
            Log.error("Failed to save user to Mongo", ex);
        }
    }

    public UserData loadUser(@NotNull final UUID uuid) {
        try {
            final Document doc = collection("users").find(new Document("_id", uuid.toString())).first();
            if (doc == null) {
                return null;
            }
            final String json = doc.toJson();
            return JsonUtil.getObjectMapper().readValue(json, UserData.class);
        } catch (Exception ex) {
            Log.error("Failed to load user from Mongo", ex);
            return null;
        }
    }

    public List<UserData> loadAllUsers() {
        final List<UserData> users = new ArrayList<>();
        try {
            for (final Document doc : collection("users").find()) {
                final String json = doc.toJson();
                final UserData user = JsonUtil.getObjectMapper().readValue(json, UserData.class);
                if (user != null) {
                    users.add(user);
                }
            }
        } catch (Exception ex) {
            Log.error("Failed to load users from Mongo", ex);
        }
        return users;
    }
}

