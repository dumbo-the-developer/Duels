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

package me.realized.duels.util.config;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.realized.duels.util.Loadable;
import me.realized.duels.util.config.convert.Converter;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class AbstractConfiguration<P extends JavaPlugin> implements Loadable {

    private static final String CONVERT_START = "[!] Converting your current configuration (%s) to the new version...";
    private static final String CONVERT_SAVE = "[!] Your old configuration was stored as %s.";
    private static final String CONVERT_DONE = "[!] Conversion complete!";

    private static final Pattern KEY_PATTERN = Pattern.compile("^([ ]*)([^ \"]+)[:].*$");
    private static final Pattern COMMENT_PATTERN = Pattern.compile("^([ ]*[#].*)|[ ]*$");

    protected final P plugin;

    private final String name;
    private final File file;

    private FileConfiguration configuration;

    public AbstractConfiguration(final P plugin, final String name) {
        this.plugin = plugin;
        this.name = name + ".yml";
        this.file = new File(plugin.getDataFolder(), this.name);
    }

    @Override
    public void handleLoad() throws IOException {
        if (!file.exists()) {
            plugin.saveResource(name, true);
        }

        loadValues(configuration = YamlConfiguration.loadConfiguration(file));
    }

    @Override
    public void handleUnload() {}

    protected abstract void loadValues(final FileConfiguration configuration) throws IOException;

    protected int getLatestVersion() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(plugin.getClass().getResourceAsStream("/" + name)))) {
            return YamlConfiguration.loadConfiguration(reader).getInt("config-version", -1);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return -1;
    }

    protected FileConfiguration convert(final Converter converter) throws IOException {
        plugin.getLogger().info(String.format(CONVERT_START, name));

        final Map<String, Object> oldValues = new HashMap<>();

        for (final String key : configuration.getKeys(true)) {
            if (key.equals("config-version")) {
                continue;
            }

            final Object value = configuration.get(key);

            if (value instanceof MemorySection) {
                continue;
            }

            oldValues.put(key, value);
        }

        if (converter != null) {
            converter.renamedKeys().forEach((old, changed) -> {
                final Object previous = oldValues.get(old);

                if (previous != null) {
                    oldValues.remove(old);
                    oldValues.put(changed, previous);
                }
            });
        }

        final String newName = name.replace(".yml", "") + "-" + System.currentTimeMillis() + ".yml";
        final File copied = Files.copy(file.toPath(), new File(plugin.getDataFolder(), newName).toPath()).toFile();
        plugin.getLogger().info(String.format(CONVERT_SAVE, copied.getName()));
        plugin.saveResource(name, true);

        // Loads comments of the new configuration file
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            final Multimap<String, List<String>> comments = LinkedListMultimap.create();
            final List<String> currentComments = new ArrayList<>();

            String line;
            Matcher matcher;

            while ((line = reader.readLine()) != null) {
                if ((matcher = KEY_PATTERN.matcher(line)).find() && !COMMENT_PATTERN.matcher(line).matches()) {
                    comments.put(matcher.group(2), Lists.newArrayList(currentComments));
                    currentComments.clear();
                } else if (COMMENT_PATTERN.matcher(line).matches()) {
                    currentComments.add(line);
                }
            }

            configuration = YamlConfiguration.loadConfiguration(file);
            configuration.options().header(null);

            // Transfer values from the old configuration
            for (Map.Entry<String, Object> entry : oldValues.entrySet()) {
                if (configuration.get(entry.getKey()) != null) {
                    configuration.set(entry.getKey(), entry.getValue());
                }
            }

            final List<String> commentlessData = Lists.newArrayList(configuration.saveToString().split("\n"));

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                for (final String data : commentlessData) {
                    matcher = KEY_PATTERN.matcher(data);

                    if (matcher.find()) {
                        final String key = matcher.group(2);
                        final Collection<List<String>> result = comments.get(key);

                        if (result != null) {
                            final List<List<String>> commentData = Lists.newArrayList(result);

                            if (!commentData.isEmpty()) {
                                for (final String comment : commentData.get(0)) {
                                    writer.write(comment);
                                    writer.newLine();
                                }

                                commentData.remove(0);
                                comments.replaceValues(key, commentData);
                            }
                        }
                    }

                    writer.write(data);

                    if (commentlessData.indexOf(data) + 1 < commentlessData.size()) {
                        writer.newLine();
                    } else if (!currentComments.isEmpty()) {
                        writer.newLine();
                    }
                }

                // Handles comments at the end of the file without any key
                for (final String comment : currentComments) {
                    writer.write(comment);

                    if (currentComments.indexOf(comment) + 1 < currentComments.size()) {
                        writer.newLine();
                    }
                }

                writer.flush();
            }

            plugin.getLogger().info(CONVERT_DONE);
        }

        return configuration;
    }
}
