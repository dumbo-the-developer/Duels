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
import me.realized._duels.data.ArenaData;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.util.Loadable;
import org.bukkit.entity.Player;

public class ArenaManager implements Loadable {

    private final DuelsPlugin plugin;
    private final File file;
    private final List<Arena> arenas = new ArrayList<>();

    public ArenaManager(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "arenas.json");
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
                for (final ArenaData arenaData : data) {
                    //
                }
            }
        }
    }

    @Override
    public void handleUnload() throws IOException {
        if (arenas.isEmpty()) {
            return;
        }

        final List<ArenaData> data = new ArrayList<>();

        for (final Arena arena : arenas) {
            // data.add(new ArenaData(arena));
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

    public boolean isInMatch(final Player player) {
        return false;
    }
}
