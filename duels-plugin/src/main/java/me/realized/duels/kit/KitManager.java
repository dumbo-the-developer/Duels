/*
 * This file is part of Duels, licensed under the MIT License.
 *
 * Copyright (c) Realized
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.Getter;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.api.event.kit.KitCreateEvent;
import me.realized.duels.api.event.kit.KitRemoveEvent;
import me.realized.duels.data.KitData;
import me.realized.duels.util.Loadable;
import me.realized.duels.util.Log;
import me.realized.duels.util.gui.MultiPageGui;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KitManager implements Loadable, me.realized.duels.api.kit.KitManager {

    private final DuelsPlugin plugin;
    private final File file;
    private final Map<String, Kit> kits = new HashMap<>();
    @Getter
    private final MultiPageGui<DuelsPlugin> gui;

    public KitManager(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "kits.json");
        // TODO: 04/06/2018 Replace to config message
        gui = new MultiPageGui<>(plugin, "Kit Selection", 1, kits.values());
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
                    kits.put(entry.getKey(), entry.getValue().toKit(plugin));
                }
            }
        }

        Log.info(this, "Loaded " + kits.size() + " kit(s).");
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

    @Nullable
    @Override
    public Kit get(@Nonnull final String name) {
        return kits.get(name);
    }

    @Nullable
    @Override
    public Kit create(@Nonnull final Player creator, @Nonnull final String name) {
        if (kits.containsKey(name)) {
            return null;
        }

        final Kit kit = new Kit(plugin, name, creator.getInventory());
        kits.put(name, kit);
        final KitCreateEvent event = new KitCreateEvent(creator, kit);
        plugin.getServer().getPluginManager().callEvent(event);
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
        return kit;
    }

    @Nullable
    @Override
    public Kit remove(@Nonnull final String name) {
        return remove(null, name);
    }

    public Kit randomKit() {
        if (kits.isEmpty()) {
            return null;
        }

        final Kit[] kits = this.kits.values().toArray(new Kit[this.kits.size()]);
        return kits[ThreadLocalRandom.current().nextInt(kits.length)];
    }
}
