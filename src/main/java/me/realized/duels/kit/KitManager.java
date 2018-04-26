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
import java.util.Optional;
import lombok.Getter;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.data.KitData;
import me.realized.duels.util.Loadable;
import me.realized.duels.util.Log;
import me.realized.duels.util.gui.MultiPageGui;
import org.bukkit.entity.Player;

public class KitManager implements Loadable {

    private final DuelsPlugin plugin;
    private final File file;
    private final Map<String, Kit> kits = new HashMap<>();
    @Getter
    private final MultiPageGui gui;

    public KitManager(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "kits.json");
        gui = new MultiPageGui("Kit Selection", 1, kits.values());
        plugin.getGuiListener().addGui(gui);
    }

    @Override
    public void handleLoad() throws IOException {
        if (!file.exists()) {
            file.createNewFile();
            return;
        }

        try (Reader reader = new InputStreamReader(new FileInputStream(file))) {
            final Map<String, KitData> data = plugin.getGson().fromJson(reader, new TypeToken<Map<String, KitData>>() {}.getType());

            if (data != null) {
                for (final Map.Entry<String, KitData> entry : data.entrySet()) {
                    kits.put(entry.getKey(), entry.getValue().toKit(plugin.getSettingCache()));
                }
            }
        }

        Log.info("Loaded " + kits.size() + " kit(s).");
        gui.calculatePages();
    }

    @Override
    public void handleUnload() throws IOException {
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

    public Optional<Kit> get(final String name) {
        return Optional.ofNullable(kits.get(name));
    }

    public Optional<Kit> remove(final String name) {
        return Optional.ofNullable(kits.remove(name));
    }

    public void save(final Player player, final String name) {
        kits.put(name, new Kit(plugin.getSettingCache(), name, player.getInventory()));
    }
}
