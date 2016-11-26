package me.realized.duels.extension;

import me.realized.duels.Core;
import me.realized.duels.utilities.Reloadable;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class ExtensionManager implements Reloadable {

    private final Map<String, DuelsExtension> extensions = new HashMap<>();

    private final Core instance;
    private final File folder;

    public ExtensionManager(Core instance) {
        this.instance = instance;

        File folder = new File(instance.getDataFolder(), "extensions");

        if (folder.mkdir()) {
            instance.info("Generated extensions folder.");
        }

        this.folder = folder;
    }

    public void load() {
        try {
            FilenameFilter filter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    int i = name.lastIndexOf('.');

                    if (i > 0) {
                        if (name.substring(i).equals(".jar")) {
                            return true;
                        }
                    }

                    return false;
                }
            };

            File[] jars = folder.listFiles(filter);

            for (File file : jars) {
                DuelsExtension extension = init(file);

                if (extension == null) {
                    continue;
                }

                if (getExtension(extension.getName()) != null) {
                    instance.warn("Failed to load extension '" + file.getName() + "': An extension with that name already exists.");
                    continue;
                }

                extensions.put(extension.getName(), extension);
            }
        } catch (Exception e) {
            instance.warn("Failed to load extensions: " + e.getMessage());
        }
    }

    public void handleDisable() {
        for (DuelsExtension extension : extensions.values()) {
            extension.setEnabled(false);
        }

        extensions.clear();
    }

    public DuelsExtension getExtension(String name) {
        return extensions.get(name);
    }

    private DuelsExtension init(File jar) {
        try {
            URL url = jar.toURI().toURL();
            URLClassLoader classLoader = new URLClassLoader(new URL[] {url}, DuelsExtension.class.getClassLoader());
            JarInputStream inputStream = new JarInputStream(url.openStream());

            Class<?> extensionClass = null;

            while (true) {
                JarEntry entry = inputStream.getNextJarEntry();

                if (entry == null) {
                    break;
                }

                String name = entry.getName();

                if (name.endsWith(".class")) {
                    name = name.replace("/", ".");
                    String className = name.substring(0, name.lastIndexOf(".class"));

                    Class<?> _class = classLoader.loadClass(className);

                    if (DuelsExtension.class.isAssignableFrom(_class)) {
                        if (extensionClass != null) {
                            instance.warn("Failed to load extension '" + jar.getName() + "': Cannot have multiple classes extending DuelsExtension.");
                            return null;
                        }

                        extensionClass = _class;
                    }
                }
            }

            if (extensionClass == null) {
                instance.warn("Failed to load extension '" + jar.getName() + "': Failed to find a class extending DuelsExtension.");
                return null;
            }

            DuelsExtension extension = ((DuelsExtension) extensionClass.newInstance());
            extension.init(instance, new File(folder, extension.getName()), jar);
            extension.setEnabled(true);
            return extension;
        } catch (Exception e) {
            instance.warn("Failed to load extension '" + jar.getName() + "': " + e.getMessage());
            return null;
        }
    }

    @Override
    public void handleReload(ReloadType type) {
        if (type == ReloadType.STRONG) {
            handleDisable();
            load();
        }
    }
}
