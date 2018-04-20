package me.realized.duels.arena;

import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.data.ArenaData;
import me.realized.duels.util.Loadable;
import me.realized.duels.util.gui.MultiPageGui;
import org.bukkit.entity.Player;

public class ArenaManager implements Loadable {

    private final DuelsPlugin plugin;
    private final File file;
    private final List<Arena> arenas = new ArrayList<>();
    @Getter
    private final MultiPageGui gui;

    public ArenaManager(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "arenas.json");
        gui = new MultiPageGui("Kit Selection", 1, arenas);
        plugin.getGuiListener().addGui(gui);
    }

    @Override
    public void handleLoad() throws IOException {
        if (!file.exists()) {
            file.createNewFile();
            return;
        }

        try (Reader reader = new InputStreamReader(new FileInputStream(file))) {
            final List<ArenaData> data = plugin.getGson().fromJson(reader, new TypeToken<List<ArenaData>>() {}.getType());

            if (data != null) {
                data.forEach(arenaData -> arenas.add(arenaData.toArena(plugin.getSettingCache())));
            }
        }

        gui.calculatePages();
    }

    @Override
    public void handleUnload() throws IOException {
        if (arenas.isEmpty()) {
            return;
        }

        final List<ArenaData> data = new ArrayList<>();

        for (final Arena arena : arenas) {
             data.add(new ArenaData(arena));
        }

        if (!file.exists()) {
            file.createNewFile();
        }

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file))) {
            writer.write(plugin.getGson().toJson(data));
            writer.flush();
        }

        arenas.clear();
    }

    public Optional<Arena> get(final String name) {
        return arenas.stream().filter(arena -> arena.getName().equals(name)).findFirst();
    }

    public boolean remove(final String name) {
        return get(name).map(arenas::remove).orElse(false);
    }

    public void save(final String name) {
        arenas.add(new Arena(plugin.getSettingCache(), name));
    }

    public boolean isInMatch(final Player player) {
        return false;
    }
}
