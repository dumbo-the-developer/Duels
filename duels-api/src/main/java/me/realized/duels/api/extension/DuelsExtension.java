package me.realized.duels.api.extension;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Objects;
import me.realized.duels.api.Duels;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class DuelsExtension {

    protected Duels api;

    private String name;
    private File folder;
    private File file;
    private File dataFolder;
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

    @NotNull
    public Duels getApi() {
        return api;
    }

    @NotNull
    public final String getName() {
        return name;
    }

    @NotNull
    public File getFolder() {
        return folder;
    }

    @NotNull
    public File getFile() {
        return file;
    }

    @NotNull
    public File getDataFolder() {
        return dataFolder;
    }

    public boolean isEnabled() {
        return enabled;
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

    public void saveResource(@NotNull String resourcePath) {
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
            File outDir = new File(dataFolder, resourcePath.substring(0, Math.max(lastIndex, 0)));

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
    public InputStream getResource(@NotNull final String filename) {
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

    /**
     * @return The version of Duels that this extension requires in order to enable.
     *
     * @deprecated As of v3.2.0. Specify 'api-version' in extension.yml instead.
     */
    @Deprecated
    @Nullable
    public String getRequiredVersion() {
        return null;
    }
}
