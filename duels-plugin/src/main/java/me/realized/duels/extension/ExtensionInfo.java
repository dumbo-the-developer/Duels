package me.realized.duels.extension;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class ExtensionInfo {

    @Getter
    private final String name, version, main, description, website;
    @Getter
    private final List<String> depends, authors;

    ExtensionInfo(final InputStream stream) throws Exception {
        try (InputStreamReader reader = new InputStreamReader(stream)) {
            final FileConfiguration config = YamlConfiguration.loadConfiguration(reader);
            this.name = getOrThrow(config, "name");
            this.version = getOrThrow(config, "version");
            this.main = getOrThrow(config, "main");
            this.description = config.getString("description", "not specified");
            this.website = config.getString("description", "not specified");
            this.depends = config.getStringList("depends");
            this.authors = config.getStringList("authors");
        }
    }

    private String getOrThrow(final FileConfiguration config, final String path) {
        final String result = config.getString(path);

        if (result == null) {
            throw new RuntimeException("No " + path + " specified in extension.yml");
        }

        return result;
    }
}
