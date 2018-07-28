package me.realized.duels.api.extension;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Objects;
import java.util.logging.Level;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.NonNull;
import me.realized.duels.api.Duels;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public abstract class DuelsExtension {

    protected Duels api;

    private String name;
    @Getter
    private File folder;
    @Getter
    private File file;
    @Getter
    private File dataFolder;

    @Getter
    private boolean enabled;

    private File configFile;
    private FileConfiguration config;

    final void init(final Duels api, final String name, final File folder, final File file) {
        this.api = api;
        this.name = name;
        this.folder = folder;
        this.file = file;
        this.dataFolder = new File(folder, name);
        this.configFile = new File(dataFolder, "config.yml");
    }

    @NonNull
    public final String getName() {
        return name;
    }

    public final void setEnabled(final boolean enabled) {
        if (this.enabled == enabled) {
            return;
        }

        if (enabled) {
            onEnable();
        } else {
            onDisable();
        }

        this.enabled = enabled;
    }

    public void saveResource(@NonNull String resourcePath) {
        Objects.requireNonNull(resourcePath, "resourcePath");
        resourcePath = resourcePath.replace('\\', '/');

        try (InputStream in = getResource(resourcePath)) {
            if (in == null) {
                throw new IllegalArgumentException("The embedded resource '" + resourcePath + "' cannot be found in " + file);
            }

            if (!dataFolder.exists()) {
                dataFolder.mkdir();
            }

            final File outFile = new File(dataFolder, resourcePath);
            int lastIndex = resourcePath.lastIndexOf('/');
            File outDir = new File(dataFolder, resourcePath.substring(0, lastIndex >= 0 ? lastIndex : 0));

            if (!outDir.exists()) {
                outDir.mkdirs();
            }

            try (OutputStream out = new FileOutputStream(outFile)) {
                final byte[] buf = new byte[1024];
                int len;

                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
        } catch (IOException ex) {
            api.error("Could not save resource '" + resourcePath + "'", ex);
        }
    }

    @Nullable
    public InputStream getResource(@NonNull final String filename) {
        Objects.requireNonNull(filename, "filename");

        try {
            final URL url = getClass().getClassLoader().getResource(filename);

            if (url == null) {
                return null;
            }

            final URLConnection connection = url.openConnection();
            connection.setUseCaches(false);
            return connection.getInputStream();
        } catch (IOException ex) {
            api.error("Could not find resource with filename '" + filename + "'", ex);
            return null;
        }
    }

    public FileConfiguration getConfig() {
        if (config == null) {
            reloadConfig();
        }

        return config;
    }

    public void reloadConfig() {
        if (!configFile.exists()) {
            saveResource("config.yml");
        }

        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public void saveConfig() {
        try {
            getConfig().save(configFile);
        } catch (IOException ex) {
            api.error("Failed to save config", ex);
        }
    }

    public void onEnable() {}

    public void onDisable() {}

    @Nullable
    public String getRequiredVersion() {
        return null;
    }
}
