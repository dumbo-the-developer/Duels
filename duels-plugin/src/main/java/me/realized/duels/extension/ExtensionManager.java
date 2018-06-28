package me.realized.duels.extension;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.api.Duels;
import me.realized.duels.api.extension.DuelsExtension;
import me.realized.duels.util.Loadable;
import me.realized.duels.util.Log;

public class ExtensionManager implements Loadable {

    private static Method INIT_EXTENSION;

    static {
        try {
            INIT_EXTENSION = DuelsExtension.class.getDeclaredMethod("init", Duels.class, File.class);
            INIT_EXTENSION.setAccessible(true);
        } catch (NoSuchMethodException ignored) {}
    }

    private final Map<String, DuelsExtension> extensions = new HashMap<>();

    private final DuelsPlugin plugin;
    private final File folder;

    public ExtensionManager(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.folder = new File(plugin.getDataFolder(), "extensions");

        if (!folder.exists()) {
            folder.mkdir();
        }
    }

    @Override
    public void handleLoad() {
        final FilenameFilter filter = (dir, name) -> {
            int i = name.lastIndexOf('.');
            return i > 0 && name.substring(i).equals(".jar");
        };

        File[] jars = folder.listFiles(filter);

        if (jars == null) {
            return;
        }

        for (final File file : jars) {
            try {
                final Class<? extends DuelsExtension> clazz = find(file);

                if (clazz == null) {
                    Log.error("Could not load extension " + file.getName() + ": No class extending DuelsExtension found");
                    continue;
                }

                final DuelsExtension extension = clazz.newInstance();

                if (extension == null) {
                    Log.error("Could not load extension " + file.getName() + ": Failed to instantiate " + clazz.getName());
                    continue;
                }

                INIT_EXTENSION.invoke(extension, plugin, file);

                if (extensions.containsKey(extension.getName())) {
                    Log.error("Could not load extension " + extension.getName() + ": An extension with same name already exists");
                    continue;
                }

                extension.setEnabled(true);
                Log.info(this, "Extension '" + extension.getName() + "' is now enabled.");
                extensions.put(extension.getName(), extension);
            } catch (Exception ex) {
                Log.error("Could not enable extension " + file.getName() + ": " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void handleUnload() {
        extensions.values().forEach(extension -> {
            try {
                extension.setEnabled(false);
                Log.info(this, "Extension '" + extension.getName() + "' is now disabled.");
            } catch (Exception ex) {
                Log.error("Could not disable extension " + extension.getName() + ": " + ex.getMessage());
            }
        });
        extensions.clear();
    }

    private Class<? extends DuelsExtension> find(final File file) throws IOException, ClassNotFoundException {
        final URL url = file.toURI().toURL();

        try (
            URLClassLoader classLoader = new URLClassLoader(new URL[] {url}, DuelsExtension.class.getClassLoader());
            JarInputStream stream = new JarInputStream(url.openStream())
        ) {
            Class<? extends DuelsExtension> foundClass = null;

            while (true) {
                final JarEntry entry = stream.getNextJarEntry();

                if (entry == null) {
                    break;
                }

                String name = entry.getName();

                if (name != null && !name.isEmpty() && name.endsWith(".class")) {
                    name = name.replace("/", ".");
                    final String className = name.substring(0, name.lastIndexOf(".class"));
                    final Class<?> clazz = classLoader.loadClass(className);

                    if (DuelsExtension.class.isAssignableFrom(clazz)) {
                        foundClass = clazz.asSubclass(DuelsExtension.class);
                    }
                }
            }

            return foundClass;
        }
    }
}
