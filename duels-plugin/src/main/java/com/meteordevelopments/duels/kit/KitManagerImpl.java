package com.meteordevelopments.duels.kit;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import lombok.Getter;
import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.api.event.kit.KitCreateEvent;
import com.meteordevelopments.duels.api.event.kit.KitRemoveEvent;
import com.meteordevelopments.duels.api.kit.Kit;
import com.meteordevelopments.duels.api.kit.KitManager;
import com.meteordevelopments.duels.config.Config;
import com.meteordevelopments.duels.config.Lang;
import com.meteordevelopments.duels.data.KitData;
import com.meteordevelopments.duels.util.Loadable;
import com.meteordevelopments.duels.util.Log;
import com.meteordevelopments.duels.util.StringUtil;
import com.meteordevelopments.duels.util.compat.Items;
import com.meteordevelopments.duels.util.gui.MultiPageGui;
import com.meteordevelopments.duels.util.inventory.ItemBuilder;
import com.meteordevelopments.duels.util.io.FileUtil;
import com.meteordevelopments.duels.util.json.JsonUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.*;

public class KitManagerImpl implements Loadable, KitManager {

    private static final String FILE_NAME = "kits.json";

    private static final String ERROR_NOT_ALPHANUMERIC = "&c&lCould not load kit %s: Name is not alphanumeric.";
    private static final String KITS_LOADED = "&2Loaded %s kit(s).";

    private final DuelsPlugin plugin;
    private final Config config;
    private final Lang lang;
    private final File file;

    private final Map<String, KitImpl> kits = new LinkedHashMap<>();

    @Getter
    private MultiPageGui<DuelsPlugin> gui;

    public KitManagerImpl(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.lang = plugin.getLang();
        this.file = new File(plugin.getDataFolder(), FILE_NAME);
    }

    @Override
    public void handleLoad() throws IOException {
        gui = new MultiPageGui<>(plugin, lang.getMessage("GUI.kit-selector.title"), config.getKitSelectorRows(), kits.values());
        gui.setSpaceFiller(Items.from(config.getKitSelectorFillerType(), config.getKitSelectorFillerData()));
        gui.setPrevButton(ItemBuilder.of(Material.PAPER).name(lang.getMessage("GUI.kit-selector.buttons.previous-page.name")).build());
        gui.setNextButton(ItemBuilder.of(Material.PAPER).name(lang.getMessage("GUI.kit-selector.buttons.next-page.name")).build());
        gui.setEmptyIndicator(ItemBuilder.of(Material.PAPER).name(lang.getMessage("GUI.kit-selector.buttons.empty.name")).build());
        plugin.getGuiListener().addGui(gui);

        // Load from MongoDB instead of file
        try {
            final var mongo = plugin.getMongoService();
            if (mongo != null) {
                final var collection = mongo.collection("kits");
                for (final org.bson.Document doc : collection.find()) {
                    final String json = doc.toJson();
                    final KitData data = JsonUtil.getObjectMapper().readValue(json, KitData.class);
                    if (data != null && StringUtil.isAlphanumeric(data.getName())) {
                        kits.put(data.getName(), data.toKit(plugin));
                    }
                }
            }
        } catch (Exception ex) {
            Log.error(this, ex.getMessage(), ex);
        }

        DuelsPlugin.sendMessage(String.format(KITS_LOADED, kits.size()));
        gui.calculatePages();
    }

    @Override
    public void handleUnload() {
        if (gui != null) {
            plugin.getGuiListener().removeGui(gui);
        }

        kits.clear();
    }

    void saveKits() {
        try {
            final var mongo = plugin.getMongoService();
            if (mongo != null) {
                final var collection = mongo.collection("kits");
                // Replace all kits: simplest approach for now
                collection.deleteMany(new org.bson.Document());
                for (final Map.Entry<String, KitImpl> entry : kits.entrySet()) {
                    final KitData kd = KitData.fromKit(entry.getValue());
                    final String json = JsonUtil.getObjectWriter().writeValueAsString(kd);
                    final org.bson.Document doc = org.bson.Document.parse(json);
                    doc.put("_id", kd.getName());
                    collection.insertOne(doc);
                }
                if (plugin.getRedisService() != null) {
                    // notify cross-server to refresh kits
                    kits.keySet().forEach(name -> plugin.getRedisService().publish(com.meteordevelopments.duels.redis.RedisService.CHANNEL_INVALIDATE_KIT, name));
                }
            }
        } catch (Exception ex) {
            Log.error(this, ex.getMessage(), ex);
        }
    }

    @Nullable
    @Override
    public KitImpl get(@NotNull final String name) {
        Objects.requireNonNull(name, "name");
        return kits.get(name);
    }

    // Called by Redis subscriber
    public void reloadKit(@NotNull final String name) {
        final var mongo = plugin.getMongoService();
        if (mongo == null) { return; }
        try {
            final var doc = mongo.collection("kits").find(new org.bson.Document("_id", name)).first();
            if (doc == null) { return; }
            final String json = doc.toJson();
            final com.meteordevelopments.duels.data.KitData data = com.meteordevelopments.duels.util.json.JsonUtil.getObjectMapper().readValue(json, com.meteordevelopments.duels.data.KitData.class);
            if (data == null) { return; }
            kits.put(name, data.toKit(plugin));
            if (gui != null) { gui.calculatePages(); }
        } catch (Exception ignored) {}
    }

    public KitImpl create(@NotNull final Player creator, @NotNull final String name, final boolean override) {
        Objects.requireNonNull(creator, "creator");
        Objects.requireNonNull(name, "name");

        if (!StringUtil.isAlphanumeric(name) || (!override && kits.containsKey(name))) {
            return null;
        }

        final KitImpl kit = new KitImpl(plugin, name, creator.getInventory());
        kits.put(name, kit);
        saveKits();

        final KitCreateEvent event = new KitCreateEvent(creator, kit);
        Bukkit.getPluginManager().callEvent(event);
        gui.calculatePages();
        return kit;
    }

    @Nullable
    @Override
    public KitImpl create(@NotNull final Player creator, @NotNull final String name) {
        return create(creator, name, false);
    }

    @Nullable
    @Override
    public KitImpl remove(@Nullable CommandSender source, @NotNull final String name) {
        Objects.requireNonNull(name, "name");

        final KitImpl kit = kits.remove(name);

        if (kit == null) {
            return null;
        }

        kit.setRemoved(true);
        plugin.getArenaManager().clearBinds(kit);
        saveKits();

        final KitRemoveEvent event = new KitRemoveEvent(source, kit);
        Bukkit.getPluginManager().callEvent(event);
        gui.calculatePages();
        return kit;
    }

    @Nullable
    @Override
    public KitImpl remove(@NotNull final String name) {
        return remove(null, name);
    }

    @NotNull
    @Override
    public List<Kit> getKits() {
        return Collections.unmodifiableList(Lists.newArrayList(kits.values()));
    }

    public List<String> getNames(final boolean nokit) {
        final List<String> names = new ArrayList<>(kits.keySet());

        if (nokit) {
            names.add("-"); // Special case: Change the nokit rating
        }

        return names;
    }
}