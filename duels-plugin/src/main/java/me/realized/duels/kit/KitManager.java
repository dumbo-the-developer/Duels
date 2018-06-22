package me.realized.duels.kit;

import com.google.gson.reflect.TypeToken;
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
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.Getter;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.api.event.kit.KitCreateEvent;
import me.realized.duels.api.event.kit.KitRemoveEvent;
import me.realized.duels.config.Lang;
import me.realized.duels.data.KitData;
import me.realized.duels.util.Loadable;
import me.realized.duels.util.Log;
import me.realized.duels.util.StringUtil;
import me.realized.duels.util.gui.MultiPageGui;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KitManager implements Loadable, me.realized.duels.api.kit.KitManager {

    private final DuelsPlugin plugin;
    private final Lang lang;
    private final File file;
    private final Map<String, Kit> kits = new HashMap<>();

    @Getter
    private MultiPageGui<DuelsPlugin> gui;

    public KitManager(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.lang = plugin.getLang();
        this.file = new File(plugin.getDataFolder(), "kits.json");
    }

    @Override
    public void handleLoad() throws IOException {
        gui = new MultiPageGui<>(plugin, lang.getMessage("GUI.kit-selector.title"), 1, kits.values());
        plugin.getGuiListener().addGui(gui);

        if (!file.exists()) {
            file.createNewFile();
        } else {
            try (Reader reader = new InputStreamReader(new FileInputStream(file))) {
                final Map<String, KitData> data = plugin.getGson().fromJson(reader, new TypeToken<Map<String, KitData>>() {}.getType());

                if (data != null) {
                    for (final Map.Entry<String, KitData> entry : data.entrySet()) {
                        if (!StringUtil.isAlphanumeric(entry.getKey())) {
                            Log.warn(this, "Excluding kit '" + entry.getKey() + "' from load: Name is not alphanumeric.");
                            continue;
                        }

                        kits.put(entry.getKey(), entry.getValue().toKit(plugin));
                    }
                }
            }
        }

        Log.info(this, "Loaded " + kits.size() + " kit(s).");
        gui.calculatePages();
    }

    @Override
    public void handleUnload() throws IOException {
        if (gui != null) {
            plugin.getGuiListener().removeGui(gui);
        }

        if (kits.isEmpty()) {
            return;
        }

        final Map<String, KitData> data = new HashMap<>();

        for (final Map.Entry<String, Kit> entry : kits.entrySet()) {
            data.put(entry.getKey(), new KitData(entry.getValue()));
        }

        if (!file.exists()) {
            file.createNewFile();
        }

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file))) {
            writer.write(plugin.getGson().toJson(data));
            writer.flush();
        }

        kits.clear();
    }

    @Nullable
    @Override
    public Kit get(@Nonnull final String name) {
        return kits.get(name);
    }

    @Nullable
    @Override
    public Kit create(@Nonnull final Player creator, @Nonnull final String name) {
        if (!StringUtil.isAlphanumeric(name) || kits.containsKey(name)) {
            return null;
        }

        final Kit kit = new Kit(plugin, name, creator.getInventory());
        kits.put(name, kit);
        final KitCreateEvent event = new KitCreateEvent(creator, kit);
        plugin.getServer().getPluginManager().callEvent(event);
        gui.calculatePages();
        return kit;
    }

    @Nullable
    @Override
    public Kit remove(@Nullable CommandSender source, @Nonnull final String name) {
        final Kit kit = kits.remove(name);

        if (kit == null) {
            return null;
        }

        final KitRemoveEvent event = new KitRemoveEvent(source, kit);
        plugin.getServer().getPluginManager().callEvent(event);
        gui.calculatePages();
        return kit;
    }

    @Nullable
    @Override
    public Kit remove(@Nonnull final String name) {
        return remove(null, name);
    }
}
