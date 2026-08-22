package com.meteordevelopments.duels.core.customkit.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Charsets;
import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.core.customkit.CustomKitImpl;
import com.meteordevelopments.duels.util.Log;
import com.meteordevelopments.duels.util.json.JsonUtil;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CustomKitStorage {

    private final DuelsPlugin plugin;
    private final File storageDir;

    public CustomKitStorage(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.storageDir = new File(plugin.getDataFolder(), "customkits");
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
    }

    public Map<UUID, List<CustomKitImpl>> loadAll() {
        final Map<UUID, List<CustomKitImpl>> result = new ConcurrentHashMap<>();
        final File[] files = storageDir.listFiles((dir, name) -> name.endsWith(".json"));

        if (files == null || files.length == 0) {
            return result;
        }

        for (final File file : files) {
            final String fileName = file.getName();
            final String uuidStr = fileName.substring(0, fileName.length() - 5);

            try {
                final UUID owner = UUID.fromString(uuidStr);
                final List<CustomKitImpl> kits = loadPlayer(owner);
                if (!kits.isEmpty()) {
                    result.put(owner, kits);
                }
            } catch (IllegalArgumentException ex) {
                Log.warn("Skipping invalid custom kit file: " + fileName);
            } catch (Exception ex) {
                Log.error("Failed to load custom kits from " + fileName + ": " + ex.getMessage(), ex);
            }
        }

        return result;
    }

    public List<CustomKitImpl> loadPlayer(final UUID owner) {
        final File file = new File(storageDir, owner.toString() + ".json");
        if (!file.exists() || file.length() == 0) {
            return new ArrayList<>();
        }

        try (final Reader reader = new InputStreamReader(new FileInputStream(file), Charsets.UTF_8)) {
            final List<CustomKitData> dataList = JsonUtil.getObjectMapper().readValue(
                    reader,
                    new TypeReference<List<CustomKitData>>() {}
            );

            if (dataList == null) {
                return new ArrayList<>();
            }

            final List<CustomKitImpl> kits = new ArrayList<>();
            for (final CustomKitData data : dataList) {
                if (data != null) {
                    kits.add(data.toCustomKit());
                }
            }
            return kits;
        } catch (Exception ex) {
            Log.error("Failed to load custom kits for player " + owner + ": " + ex.getMessage(), ex);
            return new ArrayList<>();
        }
    }

    public void savePlayer(final UUID owner, final List<CustomKitImpl> kits) {
        final List<CustomKitData> dataList = new ArrayList<>();
        for (final CustomKitImpl kit : kits) {
            dataList.add(CustomKitData.fromCustomKit(kit));
        }

        plugin.doAsync(() -> {
            final File file = new File(storageDir, owner.toString() + ".json");

            if (dataList.isEmpty()) {
                if (file.exists()) {
                    file.delete();
                }
                return;
            }

            try (final Writer writer = new OutputStreamWriter(new FileOutputStream(file), Charsets.UTF_8)) {
                JsonUtil.getObjectWriter().writeValue(writer, dataList);
                writer.flush();
            } catch (IOException ex) {
                Log.error("Failed to save custom kits for player " + owner + ": " + ex.getMessage(), ex);
            }
        });
    }
}
